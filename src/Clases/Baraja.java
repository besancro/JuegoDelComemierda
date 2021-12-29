package Clases;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Baraja {
	private List<Carta> cartas;
	
	//pre: 3<=numJugadores<=6
	//a�ade 48 cartas si el numero de jugadores es distinto de 5 y 40 en caso contrario
	public Baraja (int numJugadores)
	{
		this.cartas=new ArrayList<Carta>();
		int tama�o;
		if(numJugadores==5)
		{
			tama�o=40;
		}
		else
		{
			tama�o=48;
		}
		int i=0;
		int num=1;
		
		String palo[]= {"oros","bastos","copas","espadas"};
		int ipalo=0;
		int tama�opalos=palo.length;
		while(i<tama�o && ipalo<tama�opalos)
		{
			if(numJugadores==5 && (num==8 || num==9))
			{
				
			}
			else
			{
				this.cartas.add(new Carta(palo[ipalo],num));
				i++;
			}
			if(num==12)
			{
				num=1;
				ipalo++;
			}
			else
			{
				num++;
			}
		}
	}
	
	public String toString()
	{
		String s="Cartas: \n";
		int i=0;
		int tama�o=this.cartas.size();
		while(i<tama�o)
		{
			s=s+this.cartas.get(i).toString()+"\n";
			i++;
		}
		return s;
	}
	
	//devuelve el n�mero de cartas en la baraja
	public int numCartas()
	{
		return this.cartas.size();
	}
	
	//devuelve una carta aleatoria y la elimina de la baraja
	public Carta darCarta()
	{
		Random r= new Random();
		int i=r.nextInt(this.cartas.size()-1);
		Carta c=this.cartas.get(i);
		this.cartas.remove(i);
		return c;
	}

}
