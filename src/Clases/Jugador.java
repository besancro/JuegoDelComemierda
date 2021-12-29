package Clases;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
	private String name;
	private List<Carta> cartas;
	
	//crea el jugador de nombre name
	public Jugador(String name)
	{
		this.name=name;
		this.cartas=new ArrayList<Carta>();
	}
	
	//recibe una carta
	public void recibirCarta(Carta c)
	{
		this.cartas.add(c);
	}
	
	//pre: c!=null;
	//post: quita la carta c de sus cartas
	 public void quitarCarta(Carta c)
	 {
		 this.cartas.remove(c);
	 }
	 
	 //devuelve una lista con las cartas del jugador
	 public List<Carta> cartas()
	 {
		 return this.cartas;
	 }
	 
	 //devulve el numero de cartas del jugador
	 public int numCartas()
	 {
		 return this.cartas.size();
	 }
	 
	 //pre: c!=null
	 //post: devuelve true si el jugador tiene la carta c
	 public boolean tieneCarta(Carta c)
	 {
		 return this.cartas.contains(c);
	 }
}
