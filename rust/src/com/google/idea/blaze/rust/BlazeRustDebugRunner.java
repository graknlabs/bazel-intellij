/*
 * Copyright 2022 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.rust;

import com.google.idea.blaze.base.command.BlazeCommandName;
import com.google.idea.blaze.base.logging.EventLoggingService;
import com.google.idea.blaze.base.run.BlazeCommandRunConfiguration;
import com.google.idea.blaze.base.run.state.BlazeCommandRunConfigurationCommonState;
import com.google.idea.blaze.rust.BlazeRustRunConfigurationRunner.BlazeRustDummyRunProfileState;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import java.net.ServerSocket;
import org.rust.cargo.runconfig.BuildResult;
import org.rust.debugger.runconfig.RsDebugRunnerBase;
import org.rust.debugger.runconfig.RsDebugRunnerUtils;

/** Blaze plugin specific {@link RsDebugRunnerBase}. */
public class BlazeRustDebugRunner extends RsDebugRunnerBase {

  private RsDebugRunnerUtils debugRunnerUtils = RsDebugRunnerUtils.INSTANCE;

  @Override
  public String getRunnerId() {
    return "BlazeRustDebugRunner";
  }

  @Override
  public boolean canRun(String executorId, RunProfile profile) {
      System.out.println("BlazeRustDebugRunner: DefaultDebugExecutor.EXECUTOR_ID = " + DefaultDebugExecutor.EXECUTOR_ID + "profile: " + profile.getClass().getCanonicalName());
    if (!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)
        || !(profile instanceof BlazeCommandRunConfiguration)) {
        return false;
    }
    BlazeCommandRunConfiguration config = (BlazeCommandRunConfiguration) profile;
    BlazeCommandRunConfigurationCommonState handlerState =
        config.getHandlerStateIfType(BlazeCommandRunConfigurationCommonState.class);
    BlazeCommandName command =
        handlerState != null ? handlerState.getCommandState().getCommand() : null;
    return RustDebugUtils.canUseRustDebugger(config.getTargetKind())
        && (BlazeCommandName.TEST.equals(command) || BlazeCommandName.RUN.equals(command));
  }

  @Override
  protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment environment) {
      System.out.println("BlazeRustDebugRunner.doExecute");
    if (!(state instanceof BlazeRustDummyRunProfileState)) {
      return null;
    }
    EventLoggingService.getInstance().logEvent(getClass(), "debugging-rust");
    try {
      state = ((BlazeRustDummyRunProfileState) state).toNativeState(environment);
      return super.doExecute(state, environment);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public BuildResult.ToolchainError checkToolchainSupported(Project project, String host) {
      System.out.println("BlazeRustDebugRunner.checkToolchainSupported");
    return debugRunnerUtils.checkToolchainSupported(project, host);
  }

  @Override
  public boolean checkToolchainConfigured(Project project) {
      System.out.println("BlazeRustDebugRunner.checkToolchainConfigured");
    return debugRunnerUtils.checkToolchainConfigured(project);
  }
}
