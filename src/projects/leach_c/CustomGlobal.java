package projects.leach_c;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.sun.org.apache.xpath.internal.operations.Bool;

import projects.leach_c.nodes.nodeImplementations.LeachNode;
import sinalgo.configuration.Configuration;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Main;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

/**
 * Essa Classe armazena métodos e estados do projeto que afetam o framework.
 *
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 */

public class CustomGlobal extends AbstractCustomGlobal {
	/*
	 *  =======================================================================
	 *  =            Variaveis globais e de controle da simulação             =
	 *  =======================================================================
	 */

	/** Número de rodadas do sinalgo que são um ROUND para o Leach_c */
	public static int RODADAS_POR_ROUND = 200;

	/** Porcentagem de Cluster Heads desejada. */
	public static Double PORCENTAGEM_CH = 0.1;

	/** Número máximo de nós que um cluster pode ter. */
	public static int NUMERO_MAXIMO_DE_NOS_POR_CLUSTER = (RODADAS_POR_ROUND / 4) - 10;

	/** Formatador de números para os nós. */
	public static DecimalFormat DF = new DecimalFormat("##0.#");

	/** Cor dos nós quando não estão em nenhum cluster. */
	public final static Color BANDEIRA_LIVRE = Color.BLACK;

	/** Cor dos nós quando nós quando estão mortos. */
	public final static Color BANDEIRA_MORTO = Color.RED;

	/** Número de rodadas entre cada coleta de informação */
	public static int INTERVALO_DE_COLETA = 5;

	/** Energia que cada nó tem no inicio da simulação. */
	public static Double BATERIA_INICIAL = 10000000.0;

	/** Variavel que ontrola o nivel de saida dos dados */
	public static int OUTPUT_LEVEL = 0;

	public static int NUM_NOS_VIVOS = 0;
	
	public static String PATH_LEACH_DIR = "D:\\Downloads\\sinalgo\\src\\projects\\leach_c";

	public static String PATH_LOG_DIR = "";
	
	public static String SEPARETOR = "\\";

	/*
	 *  =======================================================================
	 *  =         Opções de Controle do Projeto em Tempo de Execução          =
	 *  =======================================================================
	 */

	/* Configuração do Nivel de LOGS */
	@AbstractCustomGlobal.CustomButton(buttonText = "Configurar: Nivel de Log")
	public void definirNivelDeLog() {

		JSlider js = new JSlider(0, 5, OUTPUT_LEVEL);
		js.setMajorTickSpacing(1);
		js.setMinorTickSpacing(1);
		js.setPreferredSize(new Dimension(600, 100));
		js.setPaintLabels(true);
		js.setPaintTicks(true);

		JOptionPane.showMessageDialog(null, js, "Nivel de LOG", JOptionPane.QUESTION_MESSAGE);

		int nivel = js.getValue();
		OUTPUT_LEVEL = nivel;
		////myOutput(0, "Nivel de Log ajustado para " + nivel + ".");

	}

	/* Configuração da duração dos Rounds LEACH */
	@AbstractCustomGlobal.CustomButton(buttonText = "Configurar: Duracao dos Rounds")
	public void definirRoundsPorRounds() {

		JSlider js = new JSlider(100, 2000, RODADAS_POR_ROUND);
		js.setMajorTickSpacing(100);
		js.setMinorTickSpacing(100);
		js.setPreferredSize(new Dimension(600, 100));
		js.setSnapToTicks(true);
		js.setPaintLabels(true);
		js.setPaintTicks(true);

		JOptionPane.showMessageDialog(null, js, "Qual o tamanho do Round Leach?", JOptionPane.QUESTION_MESSAGE);

		int rpr = js.getValue();
		RODADAS_POR_ROUND = rpr;
		////myOutput(0, "Tamanho do Round Leach ajustado para " + RODADAS_POR_ROUND + "rounds.");
	}

	/* Configuração da porcentagem de Cluster Heads desejada na simulação */

	@AbstractCustomGlobal.CustomButton(buttonText = "Configurar: % de Cluster Heads")
	public void definirPorcentagemDeCH() {

		JSlider js = new JSlider(1, 100, (int) (100 * PORCENTAGEM_CH));
		js.setMajorTickSpacing(5);
		js.setMinorTickSpacing(1);
		js.setPreferredSize(new Dimension(600, 100));
		js.setPaintLabels(true);
		js.setPaintTicks(true);

		JOptionPane.showMessageDialog(null, js, "Porcentagem de Cluster Heads desejada", JOptionPane.QUESTION_MESSAGE);

		double pct = js.getValue();

		PORCENTAGEM_CH = pct / 100.0;
		//myOutput(0, "Porcentagem de CH ajustada para  " + DF.format(PORCENTAGEM_CH * 100) + "%.");

	}

	/* Configuração da bateria inicial dos nós dada ao serem criados */
	@AbstractCustomGlobal.CustomButton(buttonText = "Configurar: Bateria Inicial")
	public void definirBateriaInicial() {

		SpinnerNumberModel snm = new SpinnerNumberModel();
		snm.setMaximum(999999999);
		snm.setMinimum(0);
		snm.setStepSize(100000);

		JSpinner js = new JSpinner(snm);
		js.setPreferredSize(new Dimension(600, 100));
		snm.setValue(BATERIA_INICIAL);
		JOptionPane.showMessageDialog(null, js, "Quantidade de Energia Inicial dos Nos", JOptionPane.QUESTION_MESSAGE);

		BATERIA_INICIAL = snm.getNumber().doubleValue();
		//myOutput(0, "Bateria inicial dos nós ajustada para " + BATERIA_INICIAL + ".");

	}

	/*
	 *  =======================================================================
	 *  =              Funcoes sobrescritas para o projeto atual.             =
	 *  =======================================================================
	 */

	/**
	 * Simulação não acaba nunca pois é possivel redefinir bateria para os nós,
	 * caso seja desejado ou necessário.
	 */
	@Override
	public boolean hasTerminated() {
		return false;
	}
	
	@Override
	public void preRound() {
		// TODO Auto-generated method stub
		Boolean stillWorking = true;
		
		if(NUM_NOS_VIVOS <= 0) {
			stillWorking = false;
		}
		
		
		if(!stillWorking) {
			Tools.stopSimulation();
		}
		
		super.preRound();
	}

	@Override
	public void postRound() {
		/*for(Node no : Tools.getNodeList()) {
			if(no instanceof LeachNode) {
				if(((LeachNode) no).getFuncao() == Funcao.ClusterHead) {
					RODADAS_VERDADEIRA++;
					break;
				}	
			}
		}
		
		System.out.println(RODADAS_VERDADEIRA);*/

		super.postRound();
	}
	
	/** Carrega as configuraçoes e define as variaveis globais */
	@Override
	public void checkProjectRequirements() {

		try {

			RODADAS_POR_ROUND = Configuration.getIntegerParameter("LeachNode/RodadasPorRound");
			PORCENTAGEM_CH = Configuration.getDoubleParameter("LeachNode/PorcentagemDeCH");
			NUMERO_MAXIMO_DE_NOS_POR_CLUSTER = Configuration.getIntegerParameter("LeachNode/NumeroMaximoDeNosPorCluster");
			INTERVALO_DE_COLETA = Configuration.getIntegerParameter("LeachNode/IntervaloDeColetaDeDados");
			BATERIA_INICIAL = Configuration.getDoubleParameter("LeachNode/BateriaInicial");
			OUTPUT_LEVEL = Configuration.getIntegerParameter("Simulacao/NivelDeInformacao");
			PATH_LEACH_DIR = Configuration.getStringParameter("PathToLeach/Path");
			PATH_LOG_DIR = Configuration.getStringParameter("PathToLog/Path");

			File tmpDir = null;
			Random rand = new Random();
			
			for(int j = 0; j < 5; j++) {
				//Gera 100 posi��es aleatorias e as salva em um arquivo
				tmpDir = new File(PATH_LEACH_DIR+SEPARETOR+"leachNodes100-"+j+".pos");
				if(!tmpDir.exists()) {
					String[] strings = new String[100];
					
					for(int i = 0; i < 100; i++) {
						strings[i] = (rand.nextDouble()* Configuration.dimX)+","+
						(rand.nextDouble()* Configuration.dimY)+
						",0.0";
					}

					printOnFile(strings,"leachNodes100-"+j);
				}
			}
			for(int j = 0; j < 5; j++) {
				//Gera 300 posi��es aleatorias e as salva em um arquivo
				tmpDir = new File(PATH_LEACH_DIR+SEPARETOR+"leachNodes300-"+j+".pos");
				if(!tmpDir.exists()) {
					String[] strings = new String[300];
					
					for(int i = 0; i < 300; i++) {
						strings[i] = (rand.nextDouble()* Configuration.dimX)+","+
						(rand.nextDouble()* Configuration.dimY)+
						",0.0";
					}

					printOnFile(strings,"leachNodes300-"+j);
				}
			}
			for(int j = 0; j < 5; j++) {
				//Gera 500 posi��es aleatorias e as salva em um arquivo
				tmpDir = new File(PATH_LEACH_DIR+SEPARETOR+"leachNodes500-"+j+".pos");
				if(!tmpDir.exists()) {
					String[] strings = new String[500];
					
					for(int i = 0; i < 500; i++) {
						strings[i] = (rand.nextDouble()* Configuration.dimX)+","+
						(rand.nextDouble()* Configuration.dimY)+
						",0.0";
					}

					printOnFile(strings,"leachNodes500-"+j);
				}
			}
			//Gera posi��o da esta��o radio base
			{
				tmpDir = new File(PATH_LEACH_DIR+SEPARETOR+"radioBase.pos");
				if(tmpDir.exists()) {
					tmpDir.delete();			
				}
				
				String[] strings = new String[1];
				
				strings[0] = (Configuration.dimX/2)+","+
				(Configuration.dimY/2)+
				",0.0";
			
				printOnFile(strings,"radioBase");
			}
			
		}catch (Exception e) {
			Main.fatalError(e.getStackTrace().toString());
		}

	}

	/*
	 *  =======================================================================
	 *  =                         Funções de auxilio                          =
	 *  =======================================================================
	 */

	public void printaNos() {
		NodeCollectionInterface listaNos = Tools.getNodeList();

		for (Node node : listaNos) {
			if(node instanceof LeachNode) {
				LeachNode aux = (LeachNode) node;
				System.out.println("---------------------------------------");
				System.out.println(node.ID);
				System.out.println(aux.getBateriaPorcentagem());
			}
		}
	}

	public void printOnFile(String[] strings, String name) throws IOException {
		String output = "#####----- start of node posiitons -----#####\n";


		for (String string : strings) {
			output += string+"\n";
		}

		FileWriter writer = new FileWriter(new File(PATH_LEACH_DIR,name+".pos"));

		writer.write(output);
		writer.close();

	}

	/** Função que exibe a saida para relatórios da simulação com base no limite */
	public static void myOutput(String texto) {
		
		File tmpDir = null;
		tmpDir = new File(PATH_LOG_DIR+SEPARETOR+"leachC500-5.txt");
		
		try {
			if(!tmpDir.exists()) {
				FileWriter writer = new FileWriter(tmpDir);
				writer.write(texto+"\n");
				writer.close();
			}else {
				FileWriter writer = new FileWriter(tmpDir,true);
				writer.write(texto+"\n");
				writer.close();
			}
		}catch (Exception e) {
			System.out.println(e);
		}
	}

}
