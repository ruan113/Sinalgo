package projects.leach_c.nodes.timers;

import projects.leach_c.nodes.messages.MsgClusterHeadDesconectado;
import projects.leach_c.nodes.nodeImplementations.LeachNode;
import sinalgo.nodes.timers.Timer;

public class TimerLiberarNos extends Timer {

	@Override
	public void fire() {
		// TODO Auto-generated method stub

		((LeachNode) node).myBroadcast(new MsgClusterHeadDesconectado());

	}
}
