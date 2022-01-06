package Clases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Jugador implements Serializable{
	private String name;
	private List<Carta> cartas;
	
	//crea un jugador
	public Jugador()
	{
		this.name="";
		this.cartas=new ArrayList<Carta>();
	}
	
	//crea el jugador de nombre name
	public Jugador(String name)
	{
		this.name=name;
		this.cartas=new ArrayList<Carta>();
	}
	
	public void setName(String nombre)
	{
		this.name=nombre;
	}
	
	public String getName()
	{
		return this.name;
	}
	//recibe una carta
	public void recibirCarta(Carta c)
	{
		
		if(c.dosDeOros())
		{
			this.cartas.add(0,c);
		}
		else
		{
			int i=0;
			if(this.numCartas()>0 && this.cartas.get(0).dosDeOros())
			{
				i++;
			}
			while(i<this.numCartas() && this.cartas.get(i).mayor(c) )
			{
				i++;
			}
			this.cartas.add(i,c);
		}
		
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
	 
	 //pre: el jugador tiene mas de dos cartas
	 //post: devuelve las dos mejores cartas del jugador y se las quita
	 public List<Carta> darMejoresDosCartas()
	 {
		 List<Carta> l=new ArrayList<Carta>();
		 l.add(this.cartas.get(0));
		 this.quitarCarta(this.cartas.get(0));
		 
		 l.add(this.cartas.get(0));
		 this.quitarCarta(this.cartas.get(0));
		 
		 return l;
	 }
	 
	 //pre: el jugador tiene mas de dos cartas
	 //post: devuelve las dos peores cartas del jugador y se las quita
	 public List<Carta> darPeoresDosCartas()
	 {
		 List<Carta> l=new ArrayList<Carta>();
		 l.add(this.cartas.get(this.numCartas()-1));
		 this.quitarCarta(this.cartas.get(this.numCartas()-1));

		 l.add(this.cartas.get(this.numCartas()-1));
		 this.quitarCarta(this.cartas.get(this.numCartas()-1));

		 return l;
	 }
		 
	 //pre: c!=null
	 //post: devuelve true si el jugador tiene la carta c
	 public boolean tieneCarta(Carta c)
	 {
		 boolean b=false;
		 int i=0;
		 while(b==false && i<this.cartas.size())
		 {
			 if(this.cartas.get(i).equals(c))
			 {
				 b=true;
			 }
			 i++;
		 }
		 
		 return b;
	 }
	 
	 //post: devuleve true si tiene la carta 3 de oros
	 public boolean empieza()
	 {
		 return this.tieneCarta(new Carta("oros", 3));
	 }
	 
	 public String toString()
	 {
		 String s= "Nombre: "+this.name+"\nCartas: ";
		 for(Carta c:this.cartas)
		 {
			 s=s+" ,"+c.toString();
		 }
		 return s;
	 }
	 
	 
}
