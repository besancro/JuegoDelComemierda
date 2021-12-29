package Sockets;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Clases.Baraja;
import Clases.Jugador;

public class Servidor {
	public static void main(String[] args)
	{
		ExecutorService pool =Executors.newCachedThreadPool();
		DataOutputStream writers[]=new DataOutputStream [6];
		DataInputStream[] readers= new DataInputStream[6];
		ObjectOutputStream[] writersObject= new ObjectOutputStream[6];
		//ObjectInputStream[] readersObject= new ObjectInputStream[6];
		
		try(ServerSocket ss= new ServerSocket(50000))
		{
			
			while(true)
			{
				Socket s=ss.accept();
				System.out.println("conexion establecida");
				try
				{
					DataOutputStream writer=new DataOutputStream( s.getOutputStream());
					DataInputStream reader = new DataInputStream(s.getInputStream());
					writer.writeBytes("Introduce el numero de jugadores (minimo 3 maximo 6): \n");
					writer.flush();
					int linea=reader.readInt();
					while(linea!=-1 && (linea<3 || linea>6))
					{
						writer.writeBytes("El numero de jugadores tiene que ser entre 3 y 6. Numero de jugadores: \n");
						linea=reader.readInt();
					}
					writer.writeBytes("Bien\n");
					int numJugadores=linea;

					Socket[] sockets= new Socket[numJugadores];

					sockets[0]=s;
					writers[0]=writer;
					readers[0]=reader;
					writersObject[0]=new ObjectOutputStream(s.getOutputStream());
					//readersObject[0]=new ObjectInputStream(s.getInputStream());

					int i=1;
					while(i<numJugadores)
					{
						sockets[i]=ss.accept();
						writers[i]=new DataOutputStream( sockets[i].getOutputStream());
						readers[i] = new DataInputStream(sockets[i].getInputStream());
						writersObject[i]=new ObjectOutputStream(sockets[i].getOutputStream());
						//readersObject[i]=new ObjectInputStream(sockets[i].getInputStream());
						i++;
					}

					String st="xxx\n"; //para hacer que todos los clientes esten en el mismo punto
					i=0;
					while(i<numJugadores)
					{
						writers[i].writeBytes(st);
						writers[i].flush();
						i++;
					}


					///crear Baraja y repartir cartas
					Baraja baraja=new Baraja(numJugadores);
					int cartasPorJugador=baraja.numCartas()/numJugadores;
					i=0;
					Jugador[] jugadores= new Jugador[numJugadores];
					String nom="Introduce el nombre: \n";
					String nombre;
					while(i<numJugadores)
					{
						writers[i].writeBytes(nom);
						nombre= readers[i].readLine();
						System.out.println(nombre);
						i++;
					}
					
					System.out.println("---------nombres--------");
					while(i<numJugadores)
					{
						repartirCartasJugador(cartasPorJugador,baraja,jugadores[i]);
						writersObject[i].writeObject(jugadores[i]);
						
					}
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					int aux=0;
					while(aux<6)
					{
						cerrar(writers[aux]);
						cerrar(readers[aux]);
						cerrar(writersObject[aux]);
						//cerrar(readersObject[aux]);
						aux++;
					}
				}
				
			}


		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			pool.shutdown();
		}

	}
	
	public static void repartirCartasJugador(int cartasPorJugador,Baraja b, Jugador j)
	{
		int i=0;
		while(i<cartasPorJugador)
		{
			j.recibirCarta(b.darCarta());
			i++;
		}
	}
	
	public static void cerrar(Closeable o)
	{
		try
		{
			if(o!=null)
			{
				o.close();
			}
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}