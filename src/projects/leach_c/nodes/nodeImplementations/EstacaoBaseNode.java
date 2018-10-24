package projects.leach_c.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import projects.leach_c.CustomGlobal;
import projects.leach_c.nodes.messages.MsgDados;
import projects.leach_c.nodes.nodeImplementations.LeachNode;
import projects.leach_c.nodes.timers.TimerEstacaoBaseSinalizar;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.runtime.AbstractCustomGlobal.CustomButton;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public class EstacaoBaseNode extends Node {

	public final static Double tempoDeSinalizacao = 100.0;
	public final static Double tempoDeEleicao = 1000.0;

	@Override
	public void handleMessages(Inbox inbox) {

		while (inbox.hasNext()) {

			Message m = inbox.next();

			if (m instanceof MsgDados) {
				System.out.println("EB " + ID + " -> Recebeu dados do CH " + inbox.getSender().ID);
				System.out.println(((MsgDados) m).dados);
			}
		}

	}

	public void ativarSinalizacaoFarol() {
		System.out.println("EB " + ID + " -> Enviando SINAL de farol com potencia máxima");
		TimerEstacaoBaseSinalizar tebs = new TimerEstacaoBaseSinalizar();
		tebs.startRelative(1, this);

	}

	public void elegeCHs() {
		NodeCollectionInterface listaNos = Tools.getNodeList();

		int num_nos = 0;
		double bateriaTotal = 0;

		//calcula a energia media
		for (Node node : listaNos) {
			if(node instanceof LeachNode && ((LeachNode) node).isVivo()) {
				LeachNode no = (LeachNode) node;
				num_nos++;
				bateriaTotal += no.getBateriaPorcentagem();
			}
		}

		double bateriaMedia = bateriaTotal/num_nos;
		Vector<Candidato> listNosCandidatos = new Vector<>();

		//elege os cluster heads
		for (Node node : listaNos) {
			if(node instanceof LeachNode && ((LeachNode) node).isVivo()) {
				LeachNode no = (LeachNode) node;

				//adiciona o ID do no na lista de candidatos
				if(no.getBateriaPorcentagem() >= bateriaMedia) {
					listNosCandidatos.add(new Candidato(no));
				}
			}
		}

		//Faz cada no votar nos melhores cluster heads
		for (Node node : listaNos) {
			//Pergunta a um no qual candidato ele prefere
			if(node instanceof LeachNode && ((LeachNode) node).isVivo() && !listNosCandidatos.contains(node)) {
				LeachNode noAtual = (LeachNode) node;
				double menorDist = Double.POSITIVE_INFINITY;
				Candidato melhorCandidato = null;

				//Procura pelo melhor candidato
				for (Candidato candidato : listNosCandidatos) {
					double dist = distancia(candidato.node, noAtual);
					if(menorDist > dist) {
						menorDist = dist;
						melhorCandidato = candidato;
					}
				}

				//Vota
				melhorCandidato.num_votos++;
			}
		}

		Vector<Candidato> listaEleitos = maisVotados(listNosCandidatos);

		if(listaEleitos.size() == 0)
			System.out.println("Erro ao validar os mais votados");

		for(Candidato candidato : listaEleitos) {
			candidato.node.transformarNoEmClusterHead();
		}

	}

	private Vector<Candidato> maisVotados(Vector<Candidato> listaCandidatos) {
		Vector<Candidato> eleitos = new Vector<>();
		int num_max_eleitos = (int) (listaCandidatos.size() * CustomGlobal.PORCENTAGEM_CH);

		if(num_max_eleitos == 0) {
			num_max_eleitos = 1;
		}

		while(eleitos.size() <= num_max_eleitos) {
			Candidato maisVotado = null;
			int maiorVoto = 0;

			for (Iterator<Candidato> iterator = listaCandidatos.iterator(); iterator.hasNext(); ) {
			    Candidato cand = iterator.next();

			    if (maiorVoto < cand.num_votos) {
			    	maisVotado = cand;
					maiorVoto = cand.num_votos;
			        iterator.remove();
			    }
			}

			eleitos.add(maisVotado);
		}

		return eleitos;
	}

	private double distancia(LeachNode n1, LeachNode n2) {
		return Math.sqrt(Math.pow(n2.getPosition().xCoord - n1.getPosition().xCoord, 2) + Math.pow(n2.getPosition().yCoord - n1.getPosition().yCoord, 2));
	}

	@Override
	public void preStep() {
		if (Global.currentTime % tempoDeEleicao == 0) {
			elegeCHs();
		}

		if (Global.currentTime % tempoDeSinalizacao == 0) {
			ativarSinalizacaoFarol();
		}
	}

	@Override
	public void init() {
		ativarSinalizacaoFarol();
	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub

		super.drawAsRoute(g, pt, highlight, (int) (40 * pt.getZoomFactor()));
		super.drawNodeAsDiskWithText(g, pt, highlight, ID + "", 14, Color.WHITE);

	}

}

class Candidato{
	public int num_votos;
	public LeachNode node;

	public Candidato(LeachNode node) {
		this.node = node;
		num_votos = 0;
	}
}
