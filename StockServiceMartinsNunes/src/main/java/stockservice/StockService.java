package stockservice;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * StockService permet d'obtenir la quantité d'un livre en stock,
 * de baisser la quantité d'un livre en stock
 * et d'augmenter la quantité d'un livre en stock 
 */
@Path("stockservice")
public class StockService 
{
	/**
	 * Pool de connexion à la base de données Redis
	 */
	private JedisPool pool;
	
	/**
	 * Permet d'obtenir depuis la base la quantité d'un livre 
	 * @param isbn Isbn du livre
	 * @return Réponse http indiquant la quantité du livre en stock
	 */
	@GET
	@Path("stock")
	@Produces("text/plain")
	public Response getStockRequest(@QueryParam("isbn") String isbn)
	{
		if (pool == null)
		{
			try 
			{
				initializeRedisConnection();
			} 
			catch (URISyntaxException e) 
			{
				Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
			}
		}
		
		String quantity = null;
		String name = null;
		Jedis jedis = pool.getResource();
		
		try 
		{
			quantity = jedis.hget(isbn, "quantity");
			name = jedis.hget(isbn, "name");
		}
		catch (Exception e)
		{
			Response.status(500).entity("Problem with request on database " + e.getMessage()).build();
		}
		finally 
		{
			pool.returnResource(jedis);
		}
		
		if (quantity == null || name == null)
		{
			return Response.status(404).entity("Book not found").build();
		}
		
		return Response.status(200).entity(name + ";" + quantity).build();
	}
	
	/**
	 * Permet de baisser la quantité d'un livre en stock
	 * @param isbn Isbn du livre
	 * @param quantity Quantité à enlever
	 * @return Réponse http 
	 */
	@POST
	@Path("decrease")
	public Response decreaseStockRequest(@QueryParam("isbn") String isbn, @QueryParam("quantity") int quantityToDecrease)
	{
		if (pool == null)
		{
			try 
			{
				initializeRedisConnection();
			} 
			catch (URISyntaxException e) 
			{
				Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
			}
		}
		
		Jedis jedis = pool.getResource();
		
		String presentQuantity = null;
		try 
		{
			presentQuantity = jedis.hget(isbn, "quantity");
			int subResult = Integer.parseInt(presentQuantity) - quantityToDecrease;
			jedis.hset(isbn, "quantity", String.valueOf(subResult));
		}
		catch (Exception e)
		{
			return Response.status(500).entity("Problem with request on database " + e.getMessage()).build();
		}
		finally 
		{
			pool.returnResource(jedis);
		}
		
		return Response.status(204).build();
	}
	
	/**
	 * Permet d'augmenter la quantité d'un livre de 10 en stock
	 * @param isbn Isbn du livre
	 * @return Réponse http 
	 */
	@POST
	@Path("increase")
	@Produces("text/plain")
	public Response increaseStockRequest(@QueryParam("isbn") String isbn)
	{
		if (pool == null)
		{
			try 
			{
				initializeRedisConnection();
			} 
			catch (URISyntaxException e) 
			{
				Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
			}
		}
		
		Jedis jedis = pool.getResource();
		
		String presentQuantity = null;
		try 
		{
			presentQuantity = jedis.hget(isbn, "quantity");
			int addResult = Integer.parseInt(presentQuantity) + 10;
			jedis.hset(isbn, "quantity", String.valueOf(addResult));
		}
		catch (Exception e)
		{
			return Response.status(500).entity("Problem with request on database " + e.getMessage()).build();
		}
		finally 
		{
			pool.returnResource(jedis);
		}
		
		return Response.status(204).build();
	}
	
	/**
	 * Permet de créer la connexion à la base de données Redis
	 * @throws URISyntaxException Problème de syntaxe sur l'URI
	 */
	private void initializeRedisConnection()
		throws URISyntaxException
	{
	    URI redisURI = new URI(System.getenv("REDISTOGO_URL"));
	    pool = new JedisPool(new JedisPoolConfig(),
            redisURI.getHost(),
            redisURI.getPort(),
            Protocol.DEFAULT_TIMEOUT,
            redisURI.getUserInfo().split(":",2)[1]);
	}

}
