package Sockets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import Clases.Baraja;
import Clases.Jugador;

public class RepartirCartas implements Callable<Boolean>{
	private Jugador jugador;
	private Baraja baraja;
	private int cartasPorJugador;
	private ObjectOutputStream writer;
	
	public RepartirCartas(Jugador jugador, Baraja baraja, int CartasPorJugador, ObjectOutputStream writer) {
		super();
		this.jugador=jugador;
		this.baraja=baraja;
		this.cartasPorJugador=CartasPorJugador;
		this.writer=writer;
	}
	
	public Boolean call() throws Exception
	{
		this.repartirCartasJugador(this.cartasPorJugador, this.baraja, this.jugador);

		writer.reset();
		writer.writeObject(jugador);
		writer.flush();
		
		return true;
	}
	
	
	
	private void repartirCartasJugador(int cartasPorJugador,Baraja b, Jugador j)
	{
		int i=0;
		while(i<cartasPorJugador)
		{
			j.recibirCarta(b.darCarta());
			i++;
		}
	}

}
