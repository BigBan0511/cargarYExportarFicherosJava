import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class eligeTuCesta {

	public static File empleTxt;

	public static void main(String[] args) {
		System.out.println("Bienvenidos al programa de Importacion y Exportacion de datos de 2ºDAM");
		menu();
	}

	//Menú del programa
	public static void menu() {
		Scanner datos = new Scanner(System.in);
		boolean valido = true;
		do {
			try {
				valido = true;
				int opcion = 0;
				System.out.println("Introduzca la operacion que desea realizar: ");
				System.out.println("1-Cargar fichero");
				System.out.println("2-Exportar datos a XML");
				datos = new Scanner(System.in);
				opcion = datos.nextInt();

				if(opcion == 1) {
					System.out.println("Vamos a cargar un fichero");
					pedirNombreFichero();
					creaEmpleados(empleTxt);
				}else if(opcion == 2){
					System.out.println("Vamos a exportar datos");
					File direcXml = new File("Subvenciones/");
					direcXml.mkdir();
					crearXmlSub25y55();
				}else {
					valido = false;
				}
			}catch(InputMismatchException e) {
				System.out.println("\nDebe introducir 1 o 2");
				System.out.println("");
				valido = false;
			}
		} while (valido == false);
		datos.close();
	}

	//Método por el que pide el nombre del fichero al usuario
	public static void pedirNombreFichero() {
		Scanner datos = new Scanner(System.in);
		boolean valido = true;
		do {
			valido = true;
			System.out.println("Introduzca el nombre del fichero:");
			String nombreFichero = datos.nextLine();

			if(nombreFichero.contains(".txt")) {
				empleTxt = new File("Incorporaciones/" + nombreFichero);
			}else if(nombreFichero.contains(".")) {
				valido = false;
				System.out.println("El archivo debe ser un .txt");
			}else if(!nombreFichero.contains(".")) {
				if(nombreFichero.equalsIgnoreCase("") || nombreFichero.equalsIgnoreCase(" ")) {
					System.out.println("El nombre elegido esta vacio, por favor, escriba algo");
					valido = false;
				}else
					empleTxt = new File("Incorporaciones/" + nombreFichero + ".txt");
			}
		} while (valido == false);
		datos.close();
	}


	//Con este método lee el txt y llama al método para escribir el .dat
	public static void creaEmpleados(File empleTxt) {
		ArrayList<Empleado> arrayEmpleados = new ArrayList<Empleado>();
		int id, edad, telefono;
		String nombre, apellido1, apellido2, dni, linea;
		char sexo;
		double salario;

		try {
			FileReader ficheroEntrada = new FileReader(empleTxt);
			BufferedReader leerEntrada = new BufferedReader(ficheroEntrada);

			while((linea = leerEntrada.readLine()) != null) {
				String[] empleadosArray = linea.split("#");
				id = Integer.parseInt(empleadosArray[0]);
				nombre = empleadosArray[1];
				apellido1 = empleadosArray[2];
				apellido2 = empleadosArray[3];
				edad = (short)Integer.parseInt(empleadosArray[4]);
				sexo = empleadosArray[5].charAt(0);
				telefono = Integer.parseInt(empleadosArray[6]);

				//Esto sirve para cambiar la coma del txt por un punto, ya que el double se reconoce como un punto en java
				String cambioDouble = empleadosArray[7];
				empleadosArray[7] = cambioDouble.replace(",", ".");
				salario = Double.parseDouble(empleadosArray[7]);

				dni = empleadosArray[8];
				Empleado emp = new Empleado(id, nombre, apellido1, apellido2, edad, sexo, telefono, salario, dni);
				arrayEmpleados.add(emp);
			}
			escribeEmpleados(arrayEmpleados);
			leerEntrada.close();
			ficheroEntrada.close();
			System.out.println("Se ha cargado el fichero");
		} catch (FileNotFoundException e) {
			System.out.println("El archivo no existe");
		} catch (IOException e) {
			System.out.println("Error al leer o encontrar el archivo");
		} catch (NumberFormatException e) {
			System.out.println("Error al convertir de String a valor numerico");
		}
	}

	//Método para importar los empleados en un .dat, Con este método escribimos el .dat
	public static void escribeEmpleados(ArrayList<Empleado> arEmp) {
		File empleadosDat = new File("Empleados/empleadosNavidad.dat");
		try {
			FileOutputStream ficheroSalida = new FileOutputStream(empleadosDat);
			ObjectOutputStream objetoSalida = new ObjectOutputStream(ficheroSalida);
			for (Empleado empleado : arEmp) {
				objetoSalida.writeObject(empleado);
			}
			objetoSalida.close();
			ficheroSalida.close();
		} catch (FileNotFoundException e) {
			System.out.println("El archivo no existe");
		} catch (IOException e) {
			System.out.println("Error al leer o encontrar el archivo");
		}
	}

	//Método para crear xml para mayores de 55 y menores de 25
	public static void crearXmlSub25y55() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation dom = builder.getDOMImplementation();
			Document documento25 = dom.createDocument(null, "Empleados", null);
			Document documento55 = dom.createDocument(null, "Empleados", null);
			documento25.setXmlVersion("1.0");
			documento55.setXmlVersion("1.0");
			Empleado emple = null;
			xmlSub25(emple, documento25);
			xmlMay55(emple, documento55);
		} catch (ParserConfigurationException e) {
			System.out.println("Error al leer el archivo o no existe");
		}
		System.out.println("Exportacion correcta");
	}

	//Método para crear xml menores de 25
	public static void xmlSub25(Empleado emple, Document documento) {
		boolean finArchivo = false;
		try {
			FileInputStream entrada = new FileInputStream(new File("Empleados/empleadosNavidad.dat"));
			ObjectInputStream leerObjeto = new ObjectInputStream(entrada);
			while(finArchivo == false) {
				try {
					emple = (Empleado) leerObjeto.readObject();
					if(emple.getEdad() < 25) {
						Element raiz = documento.createElement("empleado");
						documento.getDocumentElement().appendChild(raiz);
						crearElemento("dni", emple.getDni(), raiz, documento);
						crearElemento("nombre", emple.getNombre(), raiz, documento);
						crearElemento("apellidos", (emple.getApellido1() + " " + emple.getApellido2()), raiz, documento);
						crearElemento("edad", Integer.toString(emple.getEdad()), raiz, documento);
						crearElemento("telefono", Integer.toString(emple.getTelefono()), raiz, documento);
						crearElemento("sexo", Character.toString(emple.getSexo()), raiz, documento);
						crearElemento("sueldo", Double.toString(emple.getSalario() * 14), raiz, documento);
					}
				} catch (EOFException e) {
					finArchivo = true;
				}
			}
			leerObjeto.close();
			entrada.close();
			DOMSource sourceDom = new DOMSource(documento);
			StreamResult resultado = new StreamResult(new java.io.File("Subvenciones/Contrataciones25.xml"));
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(sourceDom, resultado);
		} catch (ClassNotFoundException | DOMException | IOException e) {
			System.out.println("ERROR");
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			System.out.println("Error al transformar");
		}
	}

	//Método para crear xml mayores de 55
	public static void xmlMay55(Empleado emple, Document documento) {
		boolean finArchivo = false;
		try {
			FileInputStream entrada = new FileInputStream(new File("Empleados/empleadosNavidad.dat"));
			ObjectInputStream leerObjeto = new ObjectInputStream(entrada);
			while(finArchivo == false) {
				try {
					emple = (Empleado) leerObjeto.readObject();
					if(emple.getEdad() > 55) {
						Element raiz = documento.createElement("empleado");
						documento.getDocumentElement().appendChild(raiz);
						crearElemento("dni", emple.getDni(), raiz, documento);
						crearElemento("nombre", emple.getNombre(), raiz, documento);
						crearElemento("apellidos", (emple.getApellido1() + " " + emple.getApellido2()), raiz, documento);
						crearElemento("edad", Integer.toString(emple.getEdad()), raiz, documento);
						crearElemento("telefono", Integer.toString(emple.getTelefono()), raiz, documento);
						crearElemento("sexo", Character.toString(emple.getSexo()), raiz, documento);
						crearElemento("sueldo", Double.toString(emple.getSalario() * 14), raiz, documento);
					}
				} catch (EOFException e) {
					finArchivo = true;
				}
			}
			leerObjeto.close();
			entrada.close();
			DOMSource sourceDom = new DOMSource(documento);
			StreamResult resultado = new StreamResult(new java.io.File("Subvenciones/Contrataciones55.xml"));
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(sourceDom, resultado);
		} catch (ClassNotFoundException | DOMException | IOException e) {
			System.out.println("ERROR");
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			System.out.println("Error al transformar");
		}
	}

	//Metodo para insertar las etiquetas de empleado
	private static void crearElemento(String datoEmpleado, String valor, Element raiz, Document documento) {
		Element elem = documento.createElement(datoEmpleado);
		Text texto = documento.createTextNode(valor);
		raiz.appendChild(elem);
		elem.appendChild(texto);
	}
}
