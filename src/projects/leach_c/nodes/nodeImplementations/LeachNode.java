/* ############################################################################
 * #                                                                          #
 * #    Arquivo: LeachNode.java                                               #
 * #                                                                          #
 * #    Funcao: Implementacao do No LeachNode que e a base do protocolo,      #
 * #            podendo assumir a posicao de Cluster Head, liderando um       #
 * #            cluster e fazendo a ponte de comunicacao entre os seus        #
 * #            Nos e uma Estacao Base                                        #
 * #                                                                          #
 * ############################################################################
 */

package projects.leach_c.nodes.nodeImplementations;

import java.awt.Color;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import projects.leach_c.CustomGlobal;
import projects.leach_c.Funcao;
import projects.leach_c.nodes.messages.MsgAfiliacaoCH;
import projects.leach_c.nodes.messages.MsgClusterHeadDesconectado;
import projects.leach_c.nodes.messages.MsgDados;
import projects.leach_c.nodes.messages.MsgEstacaoBaseFarol;
import projects.leach_c.nodes.messages.MsgInvitacao;
import projects.leach_c.nodes.messages.MsgNoDesconectado;
import projects.leach_c.nodes.messages.MsgRefuseConnection;
import projects.leach_c.nodes.messages.MsgSetupTDMA;
import projects.leach_c.nodes.timers.TimerDesconectarDeCH;
import projects.leach_c.nodes.timers.TimerInvitarNos;
import projects.leach_c.nodes.timers.TimerLiberarNos;
import projects.leach_c.nodes.timers.TimerSendMessage;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.*;

public class LeachNode extends Node {

	// Definições de projeto ==================================================

	// Dados Compartilhados ===================================================

	/** No�mero de Cluster Heads ativos. */
	public static int NUMEROS_DE_CH = 0;

	/** No�mero total de Nos na simulacao. */
	public static int NUMEROS_DE_NOS = 0;

	// Caracteristicas do No ==================================================

	/** Patente do No, como ele deve ser chamado e sera exibido. */
	private String patente;

	/** Estacao para que os Nos devem enviar seus dados quando são C.H. */
	private EstacaoBaseNode estacaoBase = null;

	/** Indica se o No possui energia para continuar trabalhando. */
	private boolean vivo = true;

	/**
	 * Indica se o No esta configurado em um Cluster e pode começar a trasmitir.
	 */
	private boolean configurado = false;

	/** Bateria atual do No. Quanto igual a 0 o No e dito Morto. */
	private Double bateria = CustomGlobal.BATERIA_INICIAL;

	/** Indica quem e o lider do cluster que o No participa. */
	private LeachNode currentClusterHead = null;

	/** No�mero de slots de tempo para a configuracao TDMA. */
	private int tamanhoTDMA = CustomGlobal.NUMERO_MAXIMO_DE_NOS_POR_CLUSTER;

	/** Slot que um No ira utilizar para saber quando transmitir */
	private int slotTDMA;

	// Caracteristicas do No quando e Cluster Head ============================

	/** Ultimo round que o No foi cluster Head */
	private int ultimoRoundComoCH = -1;

	/** Lista de Nos quando para quando o No for um Cluster Head. */
	private ArrayList<LeachNode> listaDeNos = new ArrayList<>();

	/** Armazena os dados de todo o cluster e para enviar para a Estacao Base. */
	private StringBuilder bufferCH = null;

	/** Armazena os dados do No, para enviar para um C.H. posteriormente. */
	private StringBuilder buffer = new StringBuilder();

	/** Flag para indicar se os dados do buffer ja podem ser descartados. */
	private boolean dadosEnviados = false;

	/** Cor do Cluster que o No participa atualmente. Definida pelo CH. */
	private Color bandeira;

	/*
	 * #########################################################################
	 * #                Funcoes Sobrescritas da API do SINALGO                 #
	 * #########################################################################
	 */

	@Override
	public void init() {

		NUMEROS_DE_NOS++;

		vivo = true;

		patente = "No ";
		CustomGlobal.myOutput(0, patente + ID + " criado.");

		bateria = CustomGlobal.BATERIA_INICIAL;

		ultimoRoundComoCH = (int) (-1 / CustomGlobal.PORCENTAGEM_CH);

		slotTDMA = -1;
		tamanhoTDMA = -1;
		bandeira = CustomGlobal.BANDEIRA_LIVRE;

		// Tenta iniciar os Nos como cluster Heads aleatoriamente ao serem criados.
		if (NUMEROS_DE_CH < NUMEROS_DE_NOS * CustomGlobal.PORCENTAGEM_CH && Math.random() < CustomGlobal.PORCENTAGEM_CH) {
			transformarNoEmClusterHead();
		}

	}

	/** Trata as mensagens recebidas. E encaminha para alguma funcao handler* */
	@Override
	public void handleMessages(Inbox inbox) {

		// Processa todas as mensagens na fila.

		while (inbox.hasNext()) {

			Message msg = inbox.next();

			CustomGlobal.myOutput(2, patente + " " + ID + " recebeu '" + msg.getClass().getSimpleName() + "' de "
					+ patente + inbox.getSender().ID + ".");

			// Mensagens da estacao base --------------------------------------
			if (msg instanceof MsgEstacaoBaseFarol) {
				handleMsgEstacaoBaseFarol((EstacaoBaseNode) inbox.getSender());
				continue;
			}
			// ----------------------------------------------------------------

			LeachNode sender = (LeachNode) inbox.getSender();

			if (getFuncao() == Funcao.ClusterHead) { // Cluster Head

				// ------------------------------------------------------------
				if (msg instanceof MsgAfiliacaoCH) {
					handleMsgAckInvitacao(sender);
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgNoDesconectado) {
					handleMsgNoDesconectado(sender);
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgDados) {
					handleMsgDados((MsgDados) msg, sender);
				}
				// ------------------------------------------------------------

			} else { // No COMUM

				// ------------------------------------------------------------
				if (msg instanceof MsgInvitacao) {
					handleMsgInvitacao((LeachNode) inbox.getSender());
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgClusterHeadDesconectado) {
					if (sender == getClusterHead()) {
						handleMsgDeconexaoCH();
					}
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgSetupTDMA) {
					handleMsgSetupTDMA((MsgSetupTDMA) msg, sender);
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgRefuseConnection) {
					handleMsgRefuseConnection(sender);
				}
				// ------------------------------------------------------------
				else if (msg instanceof MsgDados) {

					handleMsgDados((MsgDados) msg, sender);
				}
				// ------------------------------------------------------------

			}

		}
	}

	/** Funcao executada antes de cada passo da simulacao por cada No */
	@Override
	public void preStep() {

		if (getEnergiaRestante() <= 0) {
			matarNo();
		}

		if (isVivo()) {
			// Coleta os dados e adiciona no buffer.
			coletarInformacaoDoSensor();

			// Fica um round como ClusterHead
			if (getFuncao() == Funcao.ClusterHead) {

				/*if (ultimoRoundComoCH != getRound()) {
					transformarClusterHeadEmNo();
				}*/
				if (Global.currentTime % (CustomGlobal.RODADAS_POR_ROUND / 4) == 0) {
					transmitirDadosParaEB();
				}
			} else if (getFuncao() == Funcao.MembroDeCluster && isConfigurado()) {

				if (!getClusterHead().isVivo()) {
					setClusterHead(null);
				}

				// Testa se ja e o seu SLOT de transmissão...
				if (Global.currentTime % tamanhoTDMA == slotTDMA) {

					transmitirDadosAoCH();

				}
			}
		}
	}

	/** Funcao que desenha o No na tela */
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {

		// Margem entre o texto e o final a borda do objeto
		int borda;
		Font font;
		int fontSize;
		String textoClasse;

		if (!isVivo()) {
			borda = 1;
			fontSize = 14;
			textoClasse = "Morto " + ID;
		} else if (getFuncao() == Funcao.ClusterHead) {

			fontSize = 14;
			textoClasse = "CH " + ID + " [" + listaDeNos.size() + "]";

		} else {

			fontSize = 14;
			textoClasse = "NO " + ID + " (" + buffer.length() + ")";
		}

		borda = 4;

		font = new Font(null, 0, (int) (fontSize * pt.getZoomFactor()));
		g.setFont(font);

		String textoEnergia = CustomGlobal.DF.format(getBateriaPorcentagem()) + "%";

		FontMetrics fm = g.getFontMetrics(font);

		// *2 pois são 2 linhas d texto
		int h = (int) Math.ceil(fm.getHeight());
		int wC = (int) Math.ceil(fm.stringWidth(textoClasse));
		int wE = (int) Math.ceil(fm.stringWidth(textoEnergia));
		int maior = Math.max(h, Math.max(wC, wE));

		drawingSizeInPixels = maior;

		pt.translateToGUIPosition(getPosition());

		g.setColor(getBandeira());

		int ajuste = 6;
		int d = maior + ajuste;

		if (highlight) {
			borda += 6;
		}

		borda *= pt.getZoomFactor();

		g.setColor(getBandeira());

		int meiaBorda = borda / 2;

		// Desenha o objeto com base em sua patente e se esta vivo ou nao

		if (isVivo()) {

			if (getFuncao() == Funcao.ClusterHead) {

				g.setColor(Color.BLACK);
				g.fillOval(pt.guiX - (d / 2) - meiaBorda, pt.guiY - (d / 2) - meiaBorda, d + borda, d + borda);
				g.setColor(getBandeira());
				g.fillOval(pt.guiX - d / 2, pt.guiY - d / 2, d, d);

			} else {
				g.setColor(Color.BLACK);
				g.fillRect(pt.guiX - (d / 2) - meiaBorda, pt.guiY - (d / 2) - meiaBorda, d + borda, d + borda);
				g.setColor(getBandeira());
				g.fillRect(pt.guiX - d / 2, pt.guiY - d / 2, d, d);
			}

		} else {

			g.setColor(Color.BLACK);
			g.fillRect(pt.guiX - d / 2 - meiaBorda, pt.guiY - d / 2 - meiaBorda, d + borda, d + borda);
			g.setColor(Color.RED);
			g.fillRect(pt.guiX - d / 2, pt.guiY - d / 2, d, d);

		}

		// Desenha o status no centro do objeto.

		if (isVivo()) {
			g.setColor(Color.BLACK);
			g.drawString(textoClasse, pt.guiX - wC / 2 + 1, pt.guiY - h / 8 + 1);
			g.drawString(textoEnergia, pt.guiX - wE / 2 + 1, pt.guiY + h - h / 8 + 1);
			g.setColor(Color.WHITE);
			g.drawString(textoClasse, pt.guiX - wC / 2, pt.guiY - h / 8);
			g.drawString(textoEnergia, pt.guiX - wE / 2, pt.guiY + h - h / 8);
		} else {
			g.setColor(Color.BLACK);
			g.drawString(textoClasse, pt.guiX - wC / 2 + 1, pt.guiY - h / 8 + 1);
			g.drawString("=(", pt.guiX - wE / 2 + 1, pt.guiY + h - h / 8 + 1);
			g.setColor(Color.WHITE);
			g.drawString(textoClasse, pt.guiX - wC / 2, pt.guiY - h / 8);
			g.drawString("=(", pt.guiX - wE / 2, pt.guiY + h - h / 8);

		}

	}

	public void drawDetalhes(Graphics g, PositionTransformation pt, boolean highlight) {

	}

	/** Ações apos o passo de simulacao */
	@Override
	public void postStep() {

		if (getEnergiaRestante() <= 0) {
			matarNo();
		}

	}

	@Override
	public void neighborhoodChange() {

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {

	}

	/* 
	 * ########################################################################
	 * #                        Metodos Proprios                              #
	 * #                        ----------------                              #
	 * #                      Handler de Mensagens                            #
	 * ########################################################################
	 */

	/**
	 * Atuacao: Cluster Head<br>
	 * Descricao: Trata o sinal de aceite do convite do No para o cluster atual
	 */
	public void handleMsgAckInvitacao(LeachNode sender) {

		if (listaDeNos.size() < CustomGlobal.NUMERO_MAXIMO_DE_NOS_POR_CLUSTER) {
			listaDeNos.add(sender);

			MsgSetupTDMA m = new MsgSetupTDMA(tamanhoTDMA, listaDeNos.size());

			TimerSendMessage t = new TimerSendMessage(m, sender, false);

			t.startRelative(1, this);
		} else {

			TimerSendMessage t = new TimerSendMessage(new MsgRefuseConnection(), sender, false);
			t.startRelative(1, this);

		}
	}

	/**
	 * Atuacao: Cluster Head<br>
	 * Descricao: Trata os dados recebidos de um No e os armazena no buffer do
	 * CH.
	 */
	public void handleMsgDados(MsgDados m, LeachNode ln) {
		double custo = 0;
		if (estacaoBase != null) {
			custo = getCustoTransmissao(bufferCH.length() + m.toString().length(), estacaoBase);
		}
		if (custo < getEnergiaRestante()) {
			bufferCH.append("No " + ln.ID + ":'" + m.dados + "'\n");
			// System.out.println(bufferCH.toString());
		} else {

		}
	}

	/**
	 * Atuacao: No<br>
	 * Descricao: Trata o sinal desconexão por parte do Cluster Head
	 */
	public void handleMsgDeconexaoCH() {

		setBandeira(CustomGlobal.BANDEIRA_LIVRE);
		setClusterHead(null);
		configurado = false;
		tamanhoTDMA = -1;
		slotTDMA = -1;

	}

	/**
	 * Atuacao: Todos Nos<br>
	 * Descricao: Trata o sinal de sinalizacao emitido pelas estações base de
	 * tempo em tempo.
	 */
	public void handleMsgEstacaoBaseFarol(EstacaoBaseNode eb) {
		estacaoBase = eb;
	}

	/**
	 * Atuacao: No<br>
	 * Descricao: Trata o sinal invitacao de um CH baseado em sua distancia.
	 */
	public void handleMsgInvitacao(LeachNode ch) {

		// Se ainda nao tiver um CH aceita, caso tenha escolhe pelo
		// mais proximo.

		CustomGlobal.myOutput(3, patente + ID + " recebeu uma invitacao para se afiliar ao CH " + ch.ID);
		System.out.println("recebendo invitacao, n� "+ID);
		TimerSendMessage tsm;

		if (getClusterHead() == null || !getClusterHead().isVivo()) {

			setClusterHead(ch);

			tsm = new TimerSendMessage(new MsgAfiliacaoCH(), ch, false);
			tsm.startRelative(1, this);

		} else {

			double distanciaCHAtual = getPosition().squareDistanceTo(getClusterHead().getPosition());

			double distanciaCHNovo = getPosition().squareDistanceTo(ch.getPosition());

			CustomGlobal.myOutput(3, patente + ID + " comparou a distancia entre o CH " + getClusterHead().ID
					+ " (atual: " + distanciaCHAtual + ") e o CH " + ch.ID + " (novo: " + distanciaCHNovo + ")");

			if (distanciaCHNovo < distanciaCHAtual) {

				double diferenca = distanciaCHNovo * 100 / distanciaCHAtual;

				CustomGlobal.myOutput(3,
						patente + ID + " escolheu o CH " + ch.ID + " que esta " + CustomGlobal.DF.format(diferenca)
								+ " mais perto.");

				TimerDesconectarDeCH tdc = new TimerDesconectarDeCH(getClusterHead());
				tdc.startRelative(1, this);

				setClusterHead(ch);

				tsm = new TimerSendMessage(new MsgAfiliacaoCH(), ch, false);
				tsm.startRelative(2, this);

			} else {
				double diferenca = distanciaCHAtual * 100 / distanciaCHNovo;

				CustomGlobal.myOutput(3, patente + ID + " manteve o CH " + getClusterHead().ID + " que esta "
						+ CustomGlobal.DF.format(diferenca) + " mais perto que a nova sugestão.");

			}
		}
	}

	/**
	 * Atuacao: Cluster Head<br>
	 * Descricao: Trata mensagens de desconexão dos Nos
	 */
	public void handleMsgNoDesconectado(LeachNode sender) {
		listaDeNos.remove((LeachNode) sender);
	}

	/**
	 * Atuacao: No<br>
	 * Descricao: Trata de setup da transmissão do No
	 */
	public void handleMsgSetupTDMA(MsgSetupTDMA m, LeachNode sender) {

		CustomGlobal.myOutput(4, patente + " configuracao TDMA recebida: (divisões: " + m.tamanhoTdma + ", slot:"
				+ m.slot);

		tamanhoTDMA = m.tamanhoTdma;
		slotTDMA = m.slot;
		setClusterHead((LeachNode) sender);

		setConfigurado(true);
	}

	/**
	 * Atuacao: No<br>
	 * Descricao: Trata quando o No recebe uma mensagem de conexão rejeitada
	 */
	public void handleMsgRefuseConnection(LeachNode ch) {

		CustomGlobal.myOutput(3, patente + " conexão com CH " + ch.ID + " negada pelo mesmo.");

		if (ch == getClusterHead()) {
			setClusterHead(null);
		}

	}

	/* 
	 * ########################################################################
	 * #                        Metodos Proprios                              #
	 * #                        ----------------                              #
	 * #                         Funcoes Extras                               #
	 * ########################################################################
	 */

	/**
	 * Funcao que calcula o indice de Threshold para verificar se um No pode ou
	 * nao virar CH.
	 */
	public double T() {

		int r = getRound();

		if ((r - ultimoRoundComoCH) <= (1 / CustomGlobal.PORCENTAGEM_CH)) {
			return 0;
		}

		return CustomGlobal.PORCENTAGEM_CH
				/ (1 - CustomGlobal.PORCENTAGEM_CH * (r % (1 / CustomGlobal.PORCENTAGEM_CH)));
	}

	public boolean myBroadcast(Message m) {

		double rMax = 0;

		try {
			rMax = Configuration.getDoubleParameter("GeometricNodeCollection/rMax");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError(e.getMessage());
		}

		if (consumirEnerigia(m.toString().length() * rMax)) {
			broadcast(m);
			return true;
		}

		return false;

	}

	/**
	 * Funcao que envia mensagens para um determinado No consumindo energia com
	 * base na distancia do destino e no tamanho da mensagem
	 */

	public boolean mySend(Message m, Node target) {
		// Calcula a distancia e a potencia necessaria para enviar a mensagem

		double custo = getCustoTransmissao(m.toString().length(), target);
		if (consumirEnerigia(custo)) {

			CustomGlobal.myOutput(3, patente + ID + " enviando " + m.getClass().getSimpleName() + " para "
					+ ((LeachNode) target).patente + target.ID + " ao custo de " + custo + " J.");

			send(m, target);

			return true;

		} else {
			CustomGlobal.myOutput(4,
					patente + " " + ID + " possui apenas " + CustomGlobal.DF.format(getEnergiaRestante()) + " de "
							+ CustomGlobal.DF.format(custo)
							+ " necessarios para enviar mensagem. Mensagem NAO ENVIADA!");
			return false;
		}
	}

	/** Define o No como morto */
	public void matarNo() {

		setBandeira(CustomGlobal.BANDEIRA_LIVRE);
		tamanhoTDMA = -1;
		slotTDMA = -1;
		currentClusterHead = null;
		listaDeNos = null;
		bufferCH = null;
		buffer = null;

		vivo = false;
	}

	/** Rerorna a funcao atual do No na simulacao */
	public Funcao getFuncao() {

		if (getClusterHead() == this) {
			return Funcao.ClusterHead;
		} else if (getClusterHead() != null) {
			return Funcao.MembroDeCluster;
		}

		return Funcao.No;

	}

	/** Calcula o custo da transmissão de uma mensagem ate um No */
	public double getCustoTransmissao(int tamanhoMsg, Node destino) {

		Double custoEnergeticoPorBit = getPosition().distanceTo(destino.getPosition());

		return custoEnergeticoPorBit * tamanhoMsg * 1;

	}

	/** Transforma o No em um Cluster Head */
	public void transformarNoEmClusterHead() {

		CustomGlobal.myOutput(0, patente + ID + " virou Cluster Head");

		patente = "CH ";

		if (getFuncao() == Funcao.MembroDeCluster) {
			sinalizarDesconexaoComClusterHead();
		}

		this.setConfigurado(false);

		setClusterHead(this);

		// Incrimenta o No�mero de CH, devido ao limite de CHs ao mesmo tempo.
		NUMEROS_DE_CH++;

		// Define a cor do CH.
		int cor[] = new int[3];
		cor[0] = (int) (Math.random() * 255);
		cor[1] = (int) (Math.random() * 255);
		cor[2] = (int) (Math.random() * 255);
		setBandeira(new Color(cor[0], cor[1], cor[2]));

		// Marca o Round em que esta virando CH.
		ultimoRoundComoCH = getRound();

		setListaDeNos(new ArrayList<LeachNode>()); 

		bufferCH = new StringBuilder();

		tamanhoTDMA = CustomGlobal.NUMERO_MAXIMO_DE_NOS_POR_CLUSTER;

		// Agenda invitacao dos Nos para o Cluster Head.
		TimerInvitarNos tin = new TimerInvitarNos();

		// Agenda a invitacao para 10 rounds.
		// Isso evita conflito de mensagens de negociações anteriores.
		tin.startRelative(10, this);

	}

	/**
	 * Funcao que transforma um Cluster Head em um No normal
	 * <b>So e utilizada para fazer um CH virar um No normal.</b>
	 */
	public void transformarClusterHeadEmNo() {

		CustomGlobal.myOutput(1, patente + ID + " voltando a ser No normal.");

		transmitirDadosParaEB();

		// Envia sinal de desconexão aos seus filinhos :(
		TimerLiberarNos tln = new TimerLiberarNos();
		tln.startRelative(1, this);

		// Sinaliza que existe um CH a menos.

		NUMEROS_DE_CH--;

		// Desconfigura o cluster head
		setClusterHead(null);
		setListaDeNos(null);
		setBandeira(CustomGlobal.BANDEIRA_LIVRE);
		tamanhoTDMA = -1;
		slotTDMA = -1;

		setConfigurado(false);

	}

	public void transmitirDadosParaEB() {

		if (estacaoBase != null) {
			MsgDados m = new MsgDados(bufferCH.toString() + buffer.toString());
			TimerSendMessage tsm = new TimerSendMessage(m, estacaoBase, true);

			tsm.startRelative(1, this);
		}
	}

	public boolean isVivo() {
		return vivo;
	}

	public void sinalizarDadosEnviados() {
		dadosEnviados = true;
	}

	public void limparBuffer() {
		CustomGlobal.myOutput(5, patente + ID + " buffer limpo.");
		buffer = new StringBuilder();
	}

	public Double getBateriaPorcentagem() {
		return bateria / CustomGlobal.BATERIA_INICIAL * 100;
	}

	public void transmitirDadosAoCH() {

		if (!(slotTDMA == -1 || tamanhoTDMA == -1) && getClusterHead() != null) {
			if(getClusterHead().isVivo()) {
				MsgDados m = new MsgDados(getBuffer());
	
				TimerSendMessage tsm = new TimerSendMessage((Message) m, currentClusterHead, true);
				tsm.startRelative(1, this);
			}
		}
	}

	@NodePopupMethod(menuText = "Transformar em Cluster Head")
	public void popUpMenuTranformarClusterHead() {

		transformarNoEmClusterHead();

	}

	@NodePopupMethod(menuText = "Transformar em No Normal")
	public void popUpMenuTranformarNo() {

		transformarClusterHeadEmNo();

	}

	@NodePopupMethod(menuText = "Exibir dados do No")
	public void popUpMenuExibirDadosDoNo() {

		JOptionPane.showMessageDialog(null, this);

	}

	@NodePopupMethod(menuText = "Definir bateria no No")
	public void popUpMenuDefinirBateria() {

		String sqtd = JOptionPane.showInputDialog("Quantidade em %?");
		double qtd = Double.parseDouble(sqtd);
		if (qtd < 0) {
			JOptionPane.showMessageDialog(null, "Valor invalido!");

		} else {
			bateria = CustomGlobal.BATERIA_INICIAL * qtd / 100;
		}

	}

	public void setBateria(double energia) {
		bateria = energia;

		if (bateria > 0) {
			vivo = true;
		}
	}

	@NodePopupMethod(menuText = "Exibir dados em Buffer")
	public void popUpMenuExibirDadosEmBuffer() {

		exibirDadosEmBuffer();

	}

	public void exibirDadosEmBuffer() {
		if (buffer != null) {
			JOptionPane.showMessageDialog(null, getBuffer());
		} else {
			JOptionPane.showMessageDialog(null, "No ainda nao possui um buffer!");
		}
	}

	@NodePopupMethod(menuText = "Exibir Dados do CH")
	public void popUpMenuExibirNoCH() {
		if (bufferCH != null) {
			JOptionPane.showMessageDialog(null, getBufferCH());
		} else {
			JOptionPane.showMessageDialog(null, "No ainda nao possui um buffer de Cluster Head!");
		}

	}

	public void exibirDadosNoCH() {
		JOptionPane.showMessageDialog(null, getBufferCH());
	}

	public Color getBandeira() {
		return bandeira;
	}

	public void setBandeira(Color bandeira) {
		this.bandeira = bandeira;
	}

	public ArrayList<LeachNode> getListaDeNos() {
		return listaDeNos;
	}

	public void setListaDeNos(ArrayList<LeachNode> listaDeNos) {
		this.listaDeNos = listaDeNos;
	}

	public LeachNode getClusterHead() {
		return currentClusterHead;
	}

	public void sinalizarDesconexaoComClusterHead() {

		TimerDesconectarDeCH tddc = new TimerDesconectarDeCH(getClusterHead());
		tddc.startRelative(1, this);

	}

	public void setClusterHead(LeachNode clusterHead) {

		currentClusterHead = clusterHead;

		if (currentClusterHead != null) {
			setBandeira(getClusterHead().getBandeira());
		} else {
			setBandeira(CustomGlobal.BANDEIRA_LIVRE);
		}
	}

	public Double getEnergiaRestante() {
		return bateria;
	}

	public void limparBufferCh() {
		CustomGlobal.myOutput(5, patente + ID + " buffer de Cluster Head limpo.");
		bufferCH = new StringBuilder();
	}

	public boolean consumirEnerigia(Double j) {

		if (bateria - j > 0) {
			bateria -= j;

			CustomGlobal.myOutput(5, patente + " " + ID + " consumindo " + CustomGlobal.DF.format(j) + " energia.");
			return true;
		}
		
		matarNo();
		return false;
	}

	public boolean isClusterHead() {
		return (getClusterHead() == this);
	}

	// =========================================

	private void coletarInformacaoDoSensor() {

		if (dadosEnviados) {
			buffer = new StringBuilder();
			dadosEnviados = false;
		}

		if (Global.currentTime % CustomGlobal.INTERVALO_DE_COLETA == 0) {
			if (consumirEnerigia(1.0)) {
				buffer.append((int) (Math.random() * 10));
			}
		}
	}

	public String getBuffer() {
		return buffer.toString();
	}

	public String getBufferCH() {
		return bufferCH.toString();
	}

	public boolean isConfigurado() {
		return configurado;
	}

	public void setConfigurado(boolean configurado) {
		this.configurado = configurado;
	}

	public int getRound() {

		return (int) Global.currentTime / CustomGlobal.RODADAS_POR_ROUND;

	}

	public boolean isInicioDeRound() {
		return Global.currentTime % CustomGlobal.RODADAS_POR_ROUND == 0;
	}

	@Override
	public String toString() {
		
		String me = "";
		me += "SIMULACAO DO LEACH - STATUS DE NO�\n";
		me += "Leach Node ID:		" + ID + "\n";
		me += "Funcao:				" + getFuncao() + "\n";
		me += "Estado:				" + (isVivo() ? "vivo" : "morto") + "\n";
		me += "Cluster Head:		" + (currentClusterHead == null ? "NENHUM" : currentClusterHead.ID) + "\n";
		me += "Energia atual:		" + getBateriaPorcentagem() + "% (" + getEnergiaRestante() + ")" + "\n";
		me += "Estacao Base:		" + (estacaoBase == null ? "NAO DEFINIDO AINDA" : estacaoBase.ID) + "\n";
		me += "Buffer interno:		" + "Tamanho: " + (buffer == null ? "NULL" : buffer.length()) + " '" + (buffer == null ? "NULL" : buffer) + "'" + "\n";
		me += "Ultimo Round como CH:" + ultimoRoundComoCH + "\n";
		if (getFuncao() == Funcao.ClusterHead) {
			me += "Buffer CH:			" + "Tamanho: " + (bufferCH == null ? "NULL" : bufferCH.length()) + " '" + bufferCH + "'" + "\n";
			me += "Tamanho do Cluster:	" + listaDeNos.size();
		} else if (getFuncao() == Funcao.MembroDeCluster) {
			me += "TDMA - Tamanho:		" + tamanhoTDMA + "\n";
			me += "TDMA - Slot:			" + slotTDMA + "\n";
		}

		return me;
	}

	public String getPatente() {
		return patente;
	}

	public void setPatente(String patente) {
		this.patente = patente;
	}

}
