package Sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Cliente {

	public static void main(String[] args) {
		try(Socket s= new Socket("localhost",50000);
				//DataInputStream reader= new DataInputStream(s.getInputStream());
				DataOutputStream writer = new DataOutputStream(s.getOutputStream());
				DataInputStream teclado= new DataInputStream(System.in);
				ObjectInputStream reader= new ObjectInputStream(s.getInputStream());
				//ObjectOutputStream writeObject=new ObjectOutputStream(s.getOutputStream())
						)
		{
			String linea=reader.readLine();
			while(linea!=null && !linea.contains("Bien") && !linea.contains("xxx"))
			{
				System.out.println(linea);
				int i= Integer.parseInt(teclado.readLine());
				writer.writeInt(i);
				linea=reader.readLine();
			}
			System.out.println("hola1");
			if(linea.contains("Bien"))
			{
				linea=reader.readLine();
			}
			System.out.println("hola2");
			
			//empieza el juego
			String o=reader.readLine();
			System.out.println(o);
			String nombre=teclado.readLine();
			writer.writeBytes(nombre+"\n");
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		

	}

}
