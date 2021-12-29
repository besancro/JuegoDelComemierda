package Clases;

import java.util.List;

public class BazaCartas {
	public int numCartas; //numero de cartas que se echan a la vez
	public List<Carta> cartas;
	
	public BazaCartas()
	{
		this.numCartas=0;
	}
	
	//devuelve el numero de cartas que se estan echando a la vez
	public int numeroCartas()
	{
		return this.numCartas;
	}
	
	//pre: c!=null y c se puede echar a la mesa; 0<c.size()<=4
	//post: se echan las cartas a la mesa
	public void echarCartas(List<Carta> c)
	{
		this.cartas=c;
		this.numCartas=c.size();
	}
	
	//pre: c!=null y 0<c.size()<=4
	//post: devuelve true si se pueden echar las cartas (todas son el mismo numero y son mayores o 
	//		iguales que las que ya hay en la mesa o es el dos de oros) y false en caso contrario
	public boolean puedeEchar(List<Carta> c)
	{
		boolean b=true;
		Carta carta=c.get(0);
		int i=1;
		int tam= c.size();
		if(tam==1 && carta.dosDeOros())
		{
			b=true;
		}
		else
		{
			if(tam==this.numCartas || this.numCartas==0)
			{
				while(i<tam && b)
				{
					if(carta.getNumero()!=c.get(i).getNumero())
					{
						b=false;
					}
					i++;
				}
				
				if(this.cartas!=null && b)
				{
					if(!carta.mayor(this.cartas.get(0)))
					{
						b=false;
					}
				}
			}
			else
			{
				b=false;
			}
			
			
		}
		
		return b;
	}
}
