package projects.leach_c.nodes.timers;

import projects.leach_c.nodes.messages.MsgNoDesconectado;
import projects.leach_c.nodes.nodeImplementations.LeachNode;
import sinalgo.nodes.timers.Timer;

public class TimerDesconectarDeCH extends Timer {

	LeachNode ch;

	public TimerDesconectarDeCH(LeachNode c) {
		ch = c;
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub

		((LeachNode) node).mySend(new MsgNoDesconectado(), ch);

	}
}
