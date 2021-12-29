package Clases;

import java.util.ArrayList;
import java.util.List;

public class prueba {

	public static void main(String[] args) {
		Baraja b= new Baraja(5);
		
		//System.out.println(b.darCarta().toString());
		//System.out.println(b.toString());
		Carta c1=new Carta("oros",2);
		Carta c2= new Carta ("oros",12);
		Carta c3= new Carta ("espadas",12);
		Carta c4= new Carta ("copas",10);
		Carta c5= new Carta ("espadas",10);
//		System.out.println(c2.mayor(c1));
//		System.out.println(c1.mayor(c2));
//		System.out.println(c2.mayor(c3));
//		System.out.println(c3.mayor(c2));
		List<Carta> lc=new ArrayList();	
		lc.add(c2);
		lc.add(c3);
		List<Carta> lc1=new ArrayList();	
		lc1.add(c1);
		//lc1.add(c5);
		BazaCartas bc= new BazaCartas();
		System.out.println(bc.puedeEchar(lc));
		bc.echarCartas(lc);
		System.out.println(bc.numCartas);
		System.out.println(bc.puedeEchar(lc1));

	}

}
