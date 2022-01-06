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
						resultados.add(pool.submit(new RepartirCartasNombre(jugadores[i],baraja,cartasPorJugador,writers[i],readers[i])));
						i++;
					}
					i=0;
					while(i<numJugadores)
					{
						resultados.get(i).get();
						i++;
					}
					
					
					boolean revancha=true;
					int JugadorGanador=-1;
					int JugadorPerdedor=-1;
					
					while(revancha)//volver a jugar
					{
						
						if(JugadorGanador!=-1 && JugadorPerdedor!=-1) //para las dos mejores y peores cartas
						{
							//volvemos a repartir las cartas
							Baraja newbaraja=new Baraja(numJugadores); 
							int newcartasPorJugador=newbaraja.numCartas()/numJugadores;
							i=0;
							List<Future<Boolean>> newresultados= new ArrayList<Future<Boolean>>();
							while(i<numJugadores)
							{
								jugadores[i]=new Jugador(jugadores[i].getName());
								newresultados.add(pool.submit(new RepartirCartas(jugadores[i],newbaraja,newcartasPorJugador,writers[i])));
								i++;
							}
							i=0;
							while(i<numJugadores)
							{
								newresultados.get(i).get();
								i++;
							}
							
							//intercambiamos las cartas
							List<Carta> peores=jugadores[JugadorGanador].darPeoresDosCartas();
							List<Carta> mejores=jugadores[JugadorPerdedor].darMejoresDosCartas();
							
							for(Carta c:peores)
							{
								jugadores[JugadorPerdedor].recibirCarta(c);
							}
							
							for(Carta c:mejores)
							{
								jugadores[JugadorGanador].recibirCarta(c);
							}
							
							writers[JugadorGanador].reset();
							writers[JugadorGanador].writeBytes("Le das tus dos peores cartas a "+jugadores[JugadorPerdedor].getName()+" y recibes sus dos mejores cartas. \n");
							writers[JugadorGanador].flush();
							
							writers[JugadorGanador].reset();
							writers[JugadorGanador].writeObject(jugadores[JugadorGanador]);
							writers[JugadorGanador].flush();
							
							writers[JugadorPerdedor].reset();
							writers[JugadorPerdedor].writeBytes("Le das tus dos mejores cartas a "+jugadores[JugadorGanador].getName()+" y recibes sus dos peores cartas. \n");
							writers[JugadorPerdedor].flush();
							
							writers[JugadorPerdedor].reset();
							writers[JugadorPerdedor].writeObject(jugadores[JugadorPerdedor]);
							writers[JugadorPerdedor].flush();
						}
						
						//buscar quien empieza
						int turno=0;
						String nombre="";
						if(JugadorPerdedor!=-1)
						{
							turno=JugadorPerdedor;
							nombre=jugadores[JugadorPerdedor].getName();
						}
						else
						{
							i=0;
							while(i<numJugadores && nombre.equals(""))
							{
								if(jugadores[i].empieza())
								{
									turno=i;
									nombre=jugadores[i].getName();
								}
								i++;
							}
						}
						
						enviar(numJugadores,writers,"Empieza el jugador "+nombre+"\n");


						//-----------JUEGO-----------
						boolean fin=false;
						boolean finBaza=false;
						boolean echar;
						boolean plin=false;
						String jugar;
						List<Carta> cartas;
						BazaCartas bc;
						int ultimo=turno;
						int pasar=0;
						Jugador ganador=null;
						int sinCartas=0;

						while(!fin) //partida
						{
							plin=false;
							bc=new BazaCartas();
							if(finBaza)
							{
								
								if(jugadores[ultimo].numCartas()>0)
								{
									turno=ultimo;
								}
								finBaza=false;
								String tt="Se cierra la baza. Turno del jugador "+jugadores[turno].getName()+" \n";
								enviar(numJugadores,writers,tt);
							}
							pasar=0;
							while(!finBaza && !fin) //baza
							{
								plin=false;
								writers[turno].reset();
								writers[turno].writeObject(bc.getCartas()); //mandamos las cartas de la mesa
								writers[turno].flush();

								jugar=readers[turno].readLine();//pasar turno?

								echar=false;
								while(!jugar.contains("pasar") && !echar) //mientras que el jugador no pase o no eche cartas validas
								{
									cartas = (List<Carta>) readers[turno].readObject();
									if(bc.puedeEchar(cartas))
									{
										echar=true;
										plin=bc.plin(cartas);
										bc.echarCartas(cartas);
										pasar=0;
										ultimo=turno;
										writers[turno].reset();
										writers[turno].writeBytes("bien\n");
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
												JugadorGanador=turno;
											}

											sinCartas++;

											writers[turno].reset();
											writers[turno].writeBytes("Has quedado en el puesto: "+sinCartas+"\n");
											writers[turno].flush();
											finBaza=true;

											if(sinCartas==numJugadores-1) //si solo queda un jugador con cartas, finaliza la partida
											{
												fin=true;
											}
										}
									}
									else
									{
										writers[turno].reset();
										writers[turno].writeBytes("mal\n");
										writers[turno].flush();

										jugar= readers[turno].readLine();

									}
								}
								if(jugar.contains("pasar") || plin)
								{
									pasar++;
									if(pasar==numJugadores-sinCartas-1)
									{
										finBaza=true;
									}
								}

								int aux1=0;
								int aux2=1;
								String jugadorPlin=null;
								if(plin)
								{
									aux2++;
								}
								while(aux1<aux2) //para pasar una o dos veces dependiendo de si se ha hecho plin
								{
									boolean bien=false;//pasamos turno y comprobamos que al jugador al que le toca jugar tenga cartas
									while(!bien) {
										turno=pasarTurno(numJugadores,turno);	
										if(jugadores[turno].numCartas()>0)
										{
											bien=true;
										}
									}
									if(jugadorPlin==null)
									{
										jugadorPlin="Plin al jugador "+jugadores[turno].getName()+" \n";
									}
									aux1++;
								}

								if(!fin)
								{
									String t;
									if(plin)
									{
										enviar(numJugadores,writers,jugadorPlin);
									}
									else
									{
										t="no\n";
										enviar(numJugadores,writers,t);
									}

									if(!finBaza)
									{
										t="Turno del jugador "+jugadores[turno].getName()+" \n";
										enviar(numJugadores,writers,t);
									}

								}
							}
						}

						String finPartida="FIN\n";
						enviar(numJugadores,writers,finPartida);

						finPartida="FIN DE LA PARTIDA \n";
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
								JugadorPerdedor=i;
							}
							i++;
						}
						finPartida="Perdedor: "+perdedor.getName()+" \n";
						enviar(numJugadores,writers,finPartida);


						revancha=true;
						List<Future<Boolean>> resultadosRevancha= new ArrayList<Future<Boolean>>();
						i=0;
						while(i<numJugadores)
						{
							resultadosRevancha.add(pool.submit(new VolverAJugar(readers[i])));
							i++;
						}
						i=0;
						while(i<numJugadores)
						{
							boolean r=resultadosRevancha.get(i).get();
							if(revancha && !r)
							{
								revancha=false;
							}
							i++;
						}
						if(revancha)
						{
							enviar(numJugadores,writers,"revancha\n");
						}
						else
						{
							enviar(numJugadores,writers,"acabar\n");
						}
					}


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