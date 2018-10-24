package projects.leach_c.nodes.timers;

import projects.leach_c.CustomGlobal;
import projects.leach_c.nodes.nodeImplementations.LeachNode;

import projects.leach_c.nodes.messages.MsgInvitacao;
import sinalgo.nodes.timers.Timer;

public class TimerInvitarNos extends Timer {

	@Override
	public void fire() {
		// TODO Auto-generated method stub

		((LeachNode) node).myBroadcast(new MsgInvitacao());

		CustomGlobal.myOutput(1, ((LeachNode) node).getPatente() + " disparando invitação a todos os nós no alcance.");
	}
}
