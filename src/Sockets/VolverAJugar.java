package Sockets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import Clases.Baraja;
import Clases.Jugador;

public class VolverAJugar implements Callable<Boolean>{
	private ObjectInputStream reader;
	
	public VolverAJugar(ObjectInputStream reader) {
		super();
		this.reader=reader;
	}
	
	//devulve true si se vuelve a jugar la partida
	public Boolean call() throws Exception
	{
		String linea=reader.readLine();
		if(linea.equals("si"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
