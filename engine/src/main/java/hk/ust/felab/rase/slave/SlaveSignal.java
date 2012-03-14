package hk.ust.felab.rase.slave;

public enum SlaveSignal {
	None {
		@Override
		void action(SlaveThread slaveThread) {
		}
	},
	UpdateAlternativeSystemList {
		@Override
		void action(SlaveThread slaveThread) {
		}
	};
	abstract void action(SlaveThread slaveThread);
}
