package Sockets;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Clases.Baraja;
import Clases.BazaCartas;
import Clases.Carta;
import Clases.Jugador;

public class Servidor {
	public static void main(String[] args)
	{
		ExecutorService pool =Executors.newCachedThreadPool();
		
		ObjectInputStream[] readers= new ObjectInputStream[6];
		ObjectOutputStream writers[]=new ObjectOutputStream [6];
		
		try(ServerSocket ss= new ServerSocket(50000))
		{
			
			while(true)
			{
				Socket s=ss.accept();
				System.out.println("conexion establecida");
				try
				{
					ObjectOutputStream writer=new ObjectOutputStream( s.getOutputStream());
					ObjectInputStream reader = new ObjectInputStream(s.getInputStream());
					
					writer.reset();
					writer.writeBytes("Introduce el numero de jugadores (minimo 3 maximo 6): \n");
					writer.flush();
					
					int linea=reader.readInt();
					while(linea!=-1 && (linea<3 || linea>6))
					{
						writer.reset();
						writer.writeBytes("El numero de jugadores tiene que ser entre 3 y 6. Numero de jugadores: \n");
						writer.flush();
						linea=reader.readInt();
					}
					writer.reset();
					writer.writeBytes("Bien\n");
					writer.flush();
					int numJugadores=linea;

					Socket[] sockets= new Socket[numJugadores];

					sockets[0]=s;
					writers[0]=writer;
					readers[0]=reader;
					
					int i=1;
					while(i<numJugadores)
					{
						sockets[i]=ss.accept();
						writers[i]=new ObjectOutputStream( sockets[i].getOutputStream());
						readers[i] = new ObjectInputStream(sockets[i].getInputStream());
						
						i++;
					}
					
					//para hacer que todos los clientes esten en el mismo punto
					enviar(numJugadores,writers,"xxx\n");


					///crear Baraja y repartir cartas
					Baraja baraja=new Baraja(numJugadores);
					int cartasPorJugador=baraja.numCartas()/numJugadores;
					i=0;
					Jugador[] jugadores= new Jugador[numJugadores];
					List<Future<Boolean>> resultados= new ArrayList<Future<Boolean>>();
					while(i<numJugadores)
					{
						jugadores[i]=new Jugador();
						resultados.add(pool.submit(new RepartirCartas(jugadores[i],baraja,cartasPorJugador,writers[i],readers[i])));
						i++;
					}
					i=0;
					while(i<numJugadores)
					{
						resultados.get(i).get();
						i++;
					}
					
					
					//buscar quien empieza
					i=0;
					int turno=0;
					String nombre="";
					
					while(i<numJugadores && nombre.equals(""))
					{
						if(jugadores[i].empieza())
						{
							turno=i;
							nombre=jugadores[i].getName();
						}
						i++;
					}
					
					enviar(numJugadores,writers,"Empieza el jugador "+nombre+"\n");
					
					
					//-----------JUEGO-----------
					boolean fin=false;
					boolean finBaza=false;
					boolean echar;
					String jugar;
					List<Carta> cartas;
					BazaCartas bc;
					int ultimo=turno;
					int pasar=0;
					Jugador ganador=null;
					int sinCartas=0;
					
					while(!fin) //partida
					{
						bc=new BazaCartas();
						if(finBaza)
						{
							turno=ultimo;
							finBaza=false;
						}
						
						while(!finBaza && !fin) //baza
						{
							writers[turno].reset();
							writers[turno].writeObject(bc.getCartas()); //mandamos las cartas de la mesa
							writers[turno].flush();
							
							writers[turno].reset();
							writers[turno].writeBytes("Seleccione las cartas: \n");
							jugar=readers[turno].readLine();
							
							echar=false;
							while(!jugar.contains("pasar") && !echar) //mientras que el jugador no pase o no eche cartas validas
							{
								cartas = (List<Carta>) readers[turno].readObject();
								if(bc.puedeEchar(cartas))
								{
									echar=true;
									bc.echarCartas(cartas);
									ultimo=turno;
									writers[turno].reset();
									writers[turno].writeBytes("bien \n");
									writers[turno].flush();
									for(Carta c:cartas) //comprobamos si han echado el 2 de oros y quitamos las cartas del jugador
									{
										jugadores[turno].quitarCarta(c);
										if(c.dosDeOros())
										{
											finBaza=true;
										}
									}
									
									if(jugadores[turno].numCartas()==0)//comprobamos si el jugador ya no tiene cartas
									{
										if(ganador==null)
										{
											ganador=jugadores[turno];
										}
										
										sinCartas++;
										
										if(sinCartas==numJugadores-1) //si solo queda un jugador con cartas, finaliza la partida
										{
											fin=true;
										}
									}
								}
								else
								{
									writers[turno].reset();
									writers[turno].writeBytes("No puedes echar esas cartas. Vuelve a seleccionar las cartas o pasar el turno. \n");
									writers[turno].flush();
									
									jugar= readers[turno].readLine();
									
								}
							}
							if(jugar.contains("pasar"))
							{
								pasar++;
								if(pasar==numJugadores-sinCartas)
								{
									finBaza=true;
								}
							}
							
							
							boolean bien=false;//pasamos turno y comprobamos que al jugador al que le toca jugar tenga cartas
							while(!bien) {
								turno=pasarTurno(numJugadores,turno);	
								if(jugadores[turno].numCartas()>0)
								{
									bien=true;
								}
							}
							if(!fin)
							{
								String t="Turno del jugador "+jugadores[turno].getName()+" \n";
								enviar(numJugadores,writers,t);
							}
						}
					}
					
					String finPartida="FIN DE LA PARTIDA \n";
					enviar(numJugadores,writers,finPartida);
					finPartida="Ganador: "+ganador.getName()+" \n";
					enviar(numJugadores,writers,finPartida);
					Jugador perdedor=null;
					i=0;
					while(i<numJugadores && perdedor==null)
					{
						if(jugadores[i].numCartas()>0)
						{
							perdedor=jugadores[i];
						}
						i++;
					}
					finPartida="Perdedor: "+perdedor.getName()+" \n";
					enviar(numJugadores,writers,finPartida);

					
				}
				catch(ExecutionException e)
				{
					e.getCause().printStackTrace();
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
	
	public static void enviar(int numJugadores, ObjectOutputStream[] writers, String mensaje) throws Exception
	{
		int i=0;
		while(i<numJugadores)
		{
			writers[i].reset();
			writers[i].writeBytes(mensaje);
			writers[i].flush();
			i++;
		}
	}
	
	public static int pasarTurno(int numJugadores,int turno)
	{
		int i;
		if(turno+1<numJugadores)
		{
			i=turno+1;
		}else
		{
			i=0;
		}
		return i;
	}
	
}