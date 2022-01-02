package Sockets;

import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Clases.Carta;
import Clases.Jugador;

public class Cliente {

	public static void main(String[] args) {
		try(Socket s= new Socket("localhost",50000);
				ObjectOutputStream writer=new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream reader= new ObjectInputStream(s.getInputStream());
				DataInputStream teclado= new DataInputStream(System.in))
		{
			String linea=reader.readLine();
			while(linea!=null && !linea.contains("Bien") && !linea.contains("xxx"))
			{
				System.out.println(linea);
				int i= Integer.parseInt(teclado.readLine());
				writer.reset();
				writer.writeInt(i);
				writer.flush();
				linea=reader.readLine();
			}
			if(linea.contains("Bien"))
			{
				System.out.println("ESPERANDO A QUE SE CONECTEN LOS JUGADORES");
				linea=reader.readLine();
			}
			
			//recibir cartas y jugador
			String o=reader.readLine();
			System.out.println(o);
			String nombre=teclado.readLine();
			writer.reset();
			writer.writeBytes(nombre+"\n");
			writer.flush();
			
			Jugador j=(Jugador) reader.readObject();
			System.out.println(j.toString());
			
			//Empieza la partida
			linea=reader.readLine();
			System.out.println(linea);
			
			while(!linea.contains("FIN"))
			{
				if(linea.contains(j.getName()))//te toca jugar
				{
					System.out.println("yo");
					List<Carta> cartas= new ArrayList<Carta>();
					cartas.add(j.cartas().get(1));
					cartas.add(j.cartas().get(2));
					writer.reset();
					writer.writeObject(cartas);
					writer.flush();
				}
				linea=reader.readLine();
			}
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		

	}

}
