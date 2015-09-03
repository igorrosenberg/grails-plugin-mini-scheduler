/**
Goal: over simplified TimerService with start / stop (timerTaskName), backed by db (name, delay, status)
Unsatisfied with http://stackoverflow.com/questions/23115359/grails-scheduling-a-task-without-plugins
Inspiration from http://groovy-almanac.org/example-timer-and-timertask-in-groovy/

@see http://docs.oracle.com/javase/7/docs/api/java/util/Timer.html 
maybe @see http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html
full stack scheduler: @see https://grails.org/plugins/tag/quartz

*/

import java.util.timer.*

// Domain
class ScheduledTask {
  String name
  String frequency
  Boolean running
  
  /**
  this is the tricky part 
  1 - a (serialized) closure? 
  2 - a groovy closure, to be interpreted?
  */
  String code 
} 

class ExecClosureTask extends TimerTask {
        def closure
        String name
        ExecClosureTask(name, closure) {
           this.name = name
           this.closure = closure
        }
        public void run() {
        	log.info "Executing Task $name"
        	closure.call()
        }
}

class SchedulerService {

  def timer = new Timer()
  def timerTaskList = [:]
  
  @PostConstruct // Service 
  void postConstruct() {
    //  XXX Allow to set delay as config value
    int totalDelay = 5000   // spread start times over 5 sec.
    def list = ScheduledTask.list()
    list.eachWithIndex { domain, index -> 
      def delay = index * totalDelay / list.size()
      start(domain, delay)
    }
  }

  void start(ScheduledTask scheduledTask, delay){
      log.info "Booting ${domain.name} in $delay"
      def closure = { -> println "Executing code retrieved from DB: ${domain.code}" }
      def task = new ExecClosureTask(domain.name, closure)
      timer.scheduleAtFixedRate(task, delay, domain.frequency)
      // keep a ref on task to cancel via subsequent call
      timerTaskList[scheduledTask.id] = scheduledTask
      domain.running = true
      domain.save()
  }

  void stop(ScheduledTask scheduledTask) {
      timerTaskList[scheduledTask.id]?.cancel()
      domain.running = false
      domain.save()
  }
}

class SchedulerController {
   static scaffold = true
   
   // add start/stop methods 
   // insert button in list view
   }

