package Clases;

import java.io.Serializable;

public class Carta implements Serializable{
	private String palo;
	private int numero;
	
	//pre: palo!="" y 0<num<13 
	//post:crea una carta
	public Carta(String palo,int numero)
	{
		this.palo=palo;
		if(numero==1)
		{
			this.numero=13;
		}
		else if(numero==2)
		{
			this.numero=14;
		}
		else
		{
			this.numero=numero;
		}
		
	}
	
	public String getPalo()
	{
		return this.palo;
	}
	
	public int getNumero()
	{
		if(this.numero==13)
		{
			return 1;
		}
		else if(this.numero==14)
		{
			return 2;
		}
		else
		{
			return this.numero;
		}
	}
	
	public String toString()
	{
		String s="";
		if(this.numero==13)
		{
			s=s+1;
		}
		else if(this.numero==14)
		{
			s=s+2;
		}
		else
		{
			s=s+this.numero;
		}
		return s+" de "+this.palo;
	}
	
	
	//pre: c!= null
	//post: devuelve true si la carta en el juego del comemierda es mayor o igual que c y false en caso contrario 
	public boolean mayor(Carta c)
	{
		if(this.dosDeOros())
		{
			return true;
		}else 
		{
			return this.numero>=c.numero;
		}
	}
	
	//devuelve true si la carta es el dos de oros y false en caso contrario
	public boolean dosDeOros()
	{

		if(this.palo=="oros" && this.getNumero()==2)
		{
			return true;
		}
		else
		{
			return false;
		}

	}
	
	
	public boolean equals(Carta c)
	{
		if(this.palo.equals(c.palo) && this.numero==c.numero)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
}
