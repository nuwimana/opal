/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service.impl.quartz;

import java.text.ParseException;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.shell.service.CommandSchedulerServiceException;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz-based implementation of {@link CommandSchedulerService}.
 */
@Component
public class QuartzCommandSchedulerServiceImpl implements CommandSchedulerService {
  //
  // Instance Variables
  //

  private final Scheduler scheduler;

  //
  // Constructors
  //

  @Autowired
  public QuartzCommandSchedulerServiceImpl(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  //
  // CommandSchedulerService Methods
  //

  @Override
  public void addCommand(String name, String group, Command<?> command) {
    JobDetail jobDetail = new JobDetail(name, group, QuartzCommandJob.class);
    jobDetail.setDurability(true); // OPAL-917
    jobDetail.getJobDataMap().put("command", command.toString());
    jobDetail.getJobDataMap().put("subject", SecurityUtils.getSubject().getPrincipals());

    try {
      scheduler.addJob(jobDetail, true);
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void deleteCommand(String name, String group) {
    try {
      scheduler.deleteJob(name, group);
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void scheduleCommand(String name, String group, String cronExpression) {
    try {
      scheduler.scheduleJob(new CronTrigger(name + "-trigger", group, name, group, cronExpression));
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    } catch(ParseException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void unscheduleCommand(String name, String group) {
    try {
      Trigger[] triggers = scheduler.getTriggersOfJob(name, group);
      for(Trigger trigger : triggers) {
        scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
      }
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public String getCommandSchedule(String name, String group) {
    try {
      Trigger[] triggers = scheduler.getTriggersOfJob(name, group);
      return triggers.length != 0 && triggers[0] instanceof CronTrigger ? ((CronTrigger) triggers[0])
          .getCronExpression() : null;
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }
}
