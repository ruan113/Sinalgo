package projects.leach.models.connectivityModels;

import projects.leach.Funcao;
import projects.leach.nodes.nodeImplementations.EstacaoBaseNode;
import projects.leach.nodes.nodeImplementations.LeachNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

// Implementa a conexão entre um Cluster Head e um Nó comum.
public class LeachConnection extends ConnectivityModelHelper {

	@Override
	protected boolean isConnected(Node from, Node to) {

		// Conexão entre Leach Node e Estação Base
		// Só pode caso o LN seja um CH e esteja vivo.
		if (from instanceof LeachNode && to instanceof EstacaoBaseNode) {

			LeachNode x1 = (LeachNode) from;

			if (x1.getFuncao() == Funcao.ClusterHead && x1.isVivo() == true) {
				return true;
			}

		}

		if (from instanceof LeachNode && to instanceof LeachNode) {

			LeachNode x1 = (LeachNode) from;
			LeachNode x2 = (LeachNode) to;

			// Se um dos dois nós estiverem mortos não conecta.
			if (!x1.isVivo() || !x2.isVivo()) {
				return false;
			}

			if (x1.getFuncao() == Funcao.ClusterHead) {

				if (x2.getFuncao() == Funcao.ClusterHead) {
					return false;
				}

				// Caso o cluster Head ainda não esteja configurado aceita conexão de todos.
				if (!x1.isConfigurado()) {
					return true;
				}

				if (x2.getClusterHead() == x1) {
					return true;
				}

			}

			if (!x1.isConfigurado()) {
				return true;
			}

		}

		return false;

	}
}