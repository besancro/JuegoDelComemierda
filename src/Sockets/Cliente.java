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
				String numero= teclado.readLine();
				boolean bb=false;
				while(!bb)
				{
					if(numero.chars().allMatch(Character::isDigit))
					{
						bb=true;
					}
					else
					{
						System.out.print("Introduce un numero");
						numero=teclado.readLine();
					}
				}
				int num=Integer.parseInt(numero);
				writer.reset();
				writer.writeInt(num);
				writer.flush();
				linea=reader.readLine();
			}
			if(linea.contains("Bien"))
			{
				System.out.println("ESPERANDO A QUE SE CONECTEN LOS JUGADORES");
				linea=reader.readLine();
			}

			//recibir cartas y jugador
			String ganador="";
			String perdedor="";
			String o=reader.readLine();
			System.out.println(o);
			String nombre=teclado.readLine();
			writer.reset();
			writer.writeBytes(nombre+"\n");
			writer.flush();
			do {
				Jugador j=(Jugador) reader.readObject();
				System.out.println(j.toString());
				if(ganador.contains(j.getName()) || perdedor.contains(j.getName()))//comprobamos si has ganado o has perdido la anterior partida
				{
					linea=reader.readLine();
					System.out.println(linea);
					System.out.println();
					j=(Jugador) reader.readObject();
				}
				
				//Empieza la partida
				linea=reader.readLine();//mensaje indicando el jugador que empieza


				List<Carta> cartasBaza;
				List<Carta> misCartas;
				String lineaTeclado;
				List<Carta> mandarCarta;
				int selecCarta;

				while(!linea.equals("FIN"))
				{

					System.out.println(linea);
					if(linea.contains(j.getName()))//te toca jugar
					{
						cartasBaza= (List<Carta>) reader.readObject();
						System.out.println("Mesa: ");
						for(Carta c: cartasBaza) //mostramos las cartas de la mesa
						{
							System.out.print(c.toString()+", ");
						}
						System.out.println();

						System.out.println("Tus cartas: ");
						misCartas=j.cartas();
						int i=1;
						//for (Carta c:misCartas)
						while(i<=misCartas.size()) //mostramos nuestras cartas
						{
							//System.out.print(c.toString()+", ");
							System.out.print(i+". "+misCartas.get(i-1)+"    ");
							i++;
						}
						System.out.println();


						boolean bien=false;
						while(!bien)
						{
							System.out.println("Pasar turno? (si/no): ");
							lineaTeclado=teclado.readLine();
							if(lineaTeclado.equals("si"))
							{
								writer.reset();
								writer.writeBytes("pasar\n");
								writer.flush();
								bien=true;
							}else
							{

								System.out.println("Selecciona las cartas (introduce un 0 cuando hayas terminado): ");
								lineaTeclado=teclado.readLine();
								boolean b=false;
								while(!b)
								{
									if(lineaTeclado.chars().allMatch(Character::isDigit))
									{
										b=true;
									}
									else
									{
										System.out.print("Introduce un numero: ");
										lineaTeclado=teclado.readLine();
									}
								}
								selecCarta=Integer.parseInt(lineaTeclado);
								mandarCarta=new ArrayList<Carta>();
								while(selecCarta>0)
								{
									if(selecCarta<=misCartas.size())
									{
										mandarCarta.add(misCartas.get(selecCarta-1));
									}
									else
									{
										System.out.println("Error al seleccionar la carta. Sigue introduciendo las cartas.");
									}
									lineaTeclado=teclado.readLine();
									b=false;
									while(!b)
									{
										if(lineaTeclado.chars().allMatch(Character::isDigit))
										{
											b=true;
										}
										else
										{
											System.out.print("Introduce un numero (introduce un 0 cuando hayas terminado): ");
											lineaTeclado=teclado.readLine();
										}
									}
									selecCarta=Integer.parseInt(lineaTeclado);

								}
								if(mandarCarta.size()>0)
								{
									writer.reset();
									writer.writeBytes("jugar\n");
									writer.flush();

									writer.reset();
									writer.writeObject(mandarCarta);
									writer.flush();

									linea=reader.readLine();
									if(linea.equals("bien"))//puede echar las cartas
									{
										bien=true;
										for(Carta c: mandarCarta)
										{
											j.quitarCarta(c);
										}
										if(j.numCartas()==0)
										{
											linea=reader.readLine();
											System.out.println(linea);//mostramos el puesto en el que hemos quedado
										}
									}
									else
									{
										System.out.println("No puedes echar esas cartas.");
									}
								}
								else
								{
									System.out.println("Seleccione alguna carta o pase turno");
								}
							}
						}
					}
					linea=reader.readLine();
					if(!linea.equals("FIN"))
					{
						if(linea.contains("Plin"))
						{
							System.out.println(linea);
						}
						linea=reader.readLine();
					}

				}

				linea=reader.readLine();
				System.out.println(linea);//mensaje indicando el fin de la partida
				ganador=reader.readLine();
				System.out.println(ganador);//ganador
				perdedor=reader.readLine();
				System.out.println(perdedor);//perdedor
				
				boolean r=false;
				String revancha="";
				while(!r)
				{
					System.out.println("¿Revancha? (si/no)");
					revancha=teclado.readLine();
					if(revancha.equals("si") || revancha.equals("no"))
					{
						r=true;
					}
				}
				writer.reset();
				writer.writeBytes(revancha+"\n");
				writer.flush();
				
				linea=reader.readLine();//mensaje para saber si los demas jugadores tambien quieren volver a jugar
			}while(!linea.equals("acabar"));
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


	}

}
