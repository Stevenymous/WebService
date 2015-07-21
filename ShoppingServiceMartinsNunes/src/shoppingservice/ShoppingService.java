package shoppingservice;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.regex.*;

/**
 * ShoppingService permettant de connaître le stock d'un livre et 
 * de commander un livre en un ou plusieurs exemplaires
 */
@Path("shoppingservice")
public class ShoppingService {
	
	/**
	 * Permet de connaître la quantité d'un livre en stock
	 * @param isbn Identifie le livre et doit être valide au format ISBN-13
	 * @return Une réponse http
	 */
	@GET
	@Path("book")
	@Produces("text/plain")
	public Response bookRequest(@DefaultValue("") @QueryParam("isbn") String isbn)
	{
		if (validateIsbn(isbn))
		{
			ClientResponse responseGetStock = null;
			try 
			{
				responseGetStock = sendRequestToGetStock(isbn);
			} 
			catch (Exception e)
			{
				return Response.status(404).entity("StockService unreachable " + e.getMessage()).build();
			}
			
			String entity = (String) responseGetStock.getEntity(String.class);
			
			switch (responseGetStock.getStatus()) 
			{
				case 200:
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("Book : ").append(entity.split(";")[0])
						.append(" available in ").append(entity.split(";")[1])
						.append(" copie(s)");
					return Response.status(200).entity(stringBuilder.toString()).build();
				case 500:
					return Response.status(502).entity("Problem with StockService").build();
				default:
					return Response.status(responseGetStock.getStatus()).entity(entity).build();
			}
		}
		
		return Response.status(400).entity("Isbn invalid").build();
	}
	
	/**
	 * Permet d'acheter un livre en le désignant par son isbn et en indiquant la quantité à acheter
	 * @param isbn Identifie le livre et doit être valide au format ISBN-13
	 * @param quantity Quantité du livre à acheter
	 * @return Une réponse http
	 */
	@POST
	@Path("buy")
	@Produces("text/plain")
	public Response buyRequest(@DefaultValue("") @QueryParam("isbn") String isbn, @DefaultValue("1") @QueryParam("quantity") int quantity)
	{
		if (validateIsbn(isbn))
		{
			ClientResponse responseGetStock = null;
			ClientResponse responseDecreaseStock = null;
			ClientResponse responseOrderStock = null;
			try 
			{
				responseGetStock = sendRequestToGetStock(isbn);
			} 
			catch (Exception e)
			{
				return Response.status(404).entity("StockService unreachable " + e.getMessage()).build();
			}
			
			String entity = (String) responseGetStock.getEntity(String.class);
			String bookName = entity.split(";")[0];
			switch (responseGetStock.getStatus()) 
			{
				case 200:
					int stock = Integer.parseInt(entity.split(";")[1]);
					if (stock < quantity)
					{
						try 
						{
							responseOrderStock = sendRequestToOrder(isbn);
						} 
						catch (Exception e)
						{
							return Response.status(404).entity("StockService unreachable " + e.getMessage()).build();
						}
						switch (responseOrderStock.getStatus()) 
						{
							case 204:
								break;
							case 500:
								return Response.status(502).entity("Problem with StockService").build();
							default:
								return Response.status(responseOrderStock.getStatus()).entity("").build();
						}
					}
					
					try 
					{
						responseDecreaseStock = sendRequestToDecreaseStock(isbn, quantity);
					} 
					catch (Exception e)
					{
						return Response.status(404).entity("StockService unreachable" + e.getMessage()).build();
					}
					switch (responseDecreaseStock.getStatus()) 
					{
						case 204:
							return Response.status(200).entity("You bought '" + bookName + "' in " + quantity +" copie(s)").build();
						case 500:
							return Response.status(502).entity("Problem with StockService").build();
						default:
							return Response.status(responseDecreaseStock.getStatus()).entity("").build();
					}
					
				case 500:
					return Response.status(502).entity("Problem with StockService").build();
				default:
					return Response.status(responseGetStock.getStatus()).entity(entity).build();
			}
		}
		return Response.status(400).entity("Isbn invalid").build();
	}
	
	/**
	 * Permet de valider un isbn au format ISBN-13 par une expression régulière
	 * @param isbnToValidate L'isbn à valider
	 * @return Si l'isbn est valide ou non
	 */
	private boolean validateIsbn(String isbnToValidate)
	{
		Pattern pattern = Pattern.compile("^97[89]{1}-{1}[0-9]{1}-{1}[0-9]{4}-{1}[0-9]{4}-{1}[0-9]{1}$");
		Matcher matcher = pattern.matcher(isbnToValidate);
		
		return matcher.matches();
	}
	
	/**
	 * Permet d'envoyer une requête au stockservice pour obtenir la quantité d'un livre en stock
	 * @param isbn Isbn du livre
	 * @return La réponse http de la requête
	 * @throws Exception Erreur lors de la communication avec le stockservice
	 */
	private ClientResponse sendRequestToGetStock(String isbn)
			throws Exception
	{
		Client client = Client.create();
		WebResource webResource = client
				.resource("https://stockservicemartinsnunes.herokuapp.com/stockservice/stock?isbn=" + isbn);
		ClientResponse response = webResource.accept("text/plain").get(ClientResponse.class);
		return response;
	}
		
	/**
	 * Permet d'envoyer une requête au stockservice pour acheter un livre en "X" quantité
	 * @param isbn Isbn du livre
	 * @param quantity Quantité du livre à acheter
	 * @return La réponse http de la requête
	 * @throws Exception Erreur lors de la communication avec le stockservice
	 */
	private ClientResponse sendRequestToDecreaseStock(String isbn, int quantity)
			throws Exception
	{
		Client client = Client.create();
		WebResource webResource = client
				.resource("http://stockservicemartinsnunes.herokuapp.com/stockservice/decrease?isbn=" 
				+ isbn + "&quantity=" + Integer.valueOf(quantity));
		ClientResponse response = webResource.accept("text/plain").post(ClientResponse.class);
		return response;
	}
	
	/**
	 * Permet d'envoyer une requête au wholesalerservice pour commander des livres afin d'alimenter le stock
	 * @param isbn Isbn du livre
	 * @return La réponse http de la requête
	 * @throws Exception Erreur lors de la communication avec le wholesalerservice
	 */
	private ClientResponse sendRequestToOrder(String isbn)
			throws Exception
	{
		Client client = Client.create();
		WebResource webResource = client
				.resource("http://wholesalerservicemartinsnunes.herokuapp.com/wholesalerservice/order?isbn=" 
				+ isbn);
		ClientResponse response = webResource.accept("text/plain").post(ClientResponse.class);
		return response;
	}
	
}
