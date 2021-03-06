//========================================================================
// Licensed under the Apache License, Version 2.0 (the "License"); you may not 
// use this file except in compliance with the License.  You may obtain a copy 
// of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the 
// License for the specific language governing permissions and limitations under
// the License.
//========================================================================
package org.xtuml.bp.test.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlockDetector {

	private final DeadlockHandler deadlockHandler;
	private final long period;
	private final TimeUnit unit;
	private final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	final Runnable deadlockCheck = new Runnable() {
		@Override
		public void run() {
			long[] deadlockedThreadIds = DeadlockDetector.this.mbean.findDeadlockedThreads();

			if (deadlockedThreadIds != null) {
				ThreadInfo[] threadInfos = DeadlockDetector.this.mbean.getThreadInfo(deadlockedThreadIds);

				DeadlockDetector.this.deadlockHandler.handleDeadlock(threadInfos);				
			}
			
			ThreadInfo[] allThreadIds = DeadlockDetector.this.mbean.dumpAllThreads(true, true);
			
			DeadlockDetector.this.deadlockHandler.handleTimeExceeded(allThreadIds);
			
		}
	};

	public DeadlockDetector(final DeadlockHandler deadlockHandler, final long period, final TimeUnit unit) {
		this.deadlockHandler = deadlockHandler;
		this.period = period;
		this.unit = unit;
	}

	public void start() {
		this.scheduler.scheduleAtFixedRate(this.deadlockCheck, this.period, this.period, this.unit);
	}
	
	public void stop() {
		this.scheduler.shutdownNow();
	}
}