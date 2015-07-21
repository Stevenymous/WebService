package wholesalerservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * WholesalerService permet de commander des livres pour augmenter le stock
 */
@Path("wholesalerservice")
public class WholesalerService 
{
	/**
	 * Permet de commander des livres pour les ajouter au stock
	 * @param isbn Isbn du livre
	 * @return Requête http
	 */
	@POST
	@Path("order")
	@Produces("text/plain")
	public Response orderRequest(@QueryParam("isbn") String isbn)
	{
		Response response = null;
		try 
		{
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target("http://stockservicemartinsnunes.herokuapp.com")
					.path("stockservice/increase/").queryParam("isbn", isbn);
			response = target.request().post(null);
		} 
		catch (Exception e)
		{
			return Response.status(404).entity("StockService unreachable " + e.getMessage()).build();
		}
		
		switch (response.getStatus()) 
		{
			case 500:
				return Response.status(502).entity("Problem with StockService").build();
			default:
				return Response.status(response.getStatus()).entity(response.getEntity()).build();
		}
	}
}
