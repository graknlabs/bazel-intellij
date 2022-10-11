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
package com.google.idea.blaze.rust.run;

import com.google.idea.blaze.base.command.BlazeCommandName;
import com.google.idea.blaze.base.logging.EventLoggingService;
import com.google.idea.blaze.base.run.BlazeCommandRunConfiguration;
import com.google.idea.blaze.base.run.state.BlazeCommandRunConfigurationCommonState;
import com.google.idea.blaze.rust.run.BlazeRustRunConfigurationRunner.BlazeRustDummyRunProfileState;
import com.google.idea.blaze.rust.run.BlazeRustRunConfigurationRunner.NativeState;
import com.google.idea.blaze.rust.run.BlazeRustRunConfigurationRunner.RustExecutionInfo;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.XDebugSession;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
//import static org.jetbrains.concurrency.resolvedPromise;
import org.rust.cargo.runconfig.BuildResult;
import org.rust.cargo.runconfig.CargoRunStateBase;
import org.rust.cargo.runconfig.CargoTestRunState;
import org.rust.cargo.runconfig.RsExecutableRunner;
import org.rust.cargo.toolchain.impl.CargoMetadata;
import org.rust.cargo.toolchain.impl.CompilerArtifactMessage;
import org.rust.cargo.toolchain.impl.Profile;
import org.rust.debugger.runconfig.RsDebugRunnerBase;
import org.rust.debugger.runconfig.RsDebugRunnerUtils;

/** Blaze plugin specific {@link RsDebugRunnerBase}. */
public class BlazeRustDebugRunner extends RsDebugRunnerBase {

//    static final Key<CompletableFuture<List<CompilerArtifactMessage>>> ARTIFACTS_KEY;

//    static {
//        try {
//            System.out.println("BlazeRustDebugRunner.init (fields): " + Arrays.asList(RsExecutableRunner.Companion.getClass().getDeclaredFields()).stream().map(field -> field.getName()).collect(Collectors.toList()));
//            System.out.println("BlazeRustDebugRunner.init (methods): " + Arrays.asList(RsExecutableRunner.Companion.getClass().getDeclaredMethods()).stream().map(field -> field.getName()).collect(Collectors.toList()));
//            Field f = RsExecutableRunner.Companion.getClass().getDeclaredField("ARTIFACTS");
//            f.setAccessible(true);
//            ARTIFACTS_KEY = (Key<CompletableFuture<List<CompilerArtifactMessage>>>) f.get(null);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

  private RsDebugRunnerUtils debugRunnerUtils = RsDebugRunnerUtils.INSTANCE;

  @Override
  public String getRunnerId() {
    return "BlazeRustDebugRunner";
  }

  @Override
  public boolean canRun(String executorId, RunProfile profile) {
//      System.out.println("BlazeRustDebugRunner: DefaultDebugExecutor.EXECUTOR_ID = " + DefaultDebugExecutor.EXECUTOR_ID + ", profile: " + profile.getClass().getCanonicalName());
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
  public void execute(ExecutionEnvironment env) {
      try {
          RunProfileState state = env.getState();
          if (!(state instanceof BlazeRustDummyRunProfileState)) {
              Messages.showErrorDialog(env.getProject(), "ExecutionEnvironment state is not a BlazeRustDummyRunProfileState", "Unable to run debugger");
          }
          super.execute(env);
//          doExecute((BlazeRustDummyRunProfileState) state, env);
      } catch (ExecutionException e) {
          e.printStackTrace();
          Messages.showErrorDialog(env.getProject(), "ExecutionException thrown by ExecutionEnvironment.getState()", "Unable to run debugger");
      }
  }

  @Override
  protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment env) {
      System.out.println("BlazeRustDebugRunner.doExecute");
    if (!(state instanceof BlazeRustDummyRunProfileState)) {
      return null;
    }
    EventLoggingService.getInstance().logEvent(getClass(), "debugging-rust");
    try {
      NativeState nativeState = ((BlazeRustDummyRunProfileState) state).toNativeState(env);
        RustExecutionInfo executionInfo = env.getCopyableUserData(BlazeRustRunConfigurationRunner.EXECUTABLE_KEY).get();
        CompilerArtifactMessage compilerArtifact = new CompilerArtifactMessage(
                /* package_id = */ "typedb-client 0.0.1 (path+file:///Users/aw/Desktop/workspace/typedb-client-rust)",
                /* target = */ new CargoMetadata.Target(
                        /* kind = */ Arrays.asList("test"),
                        /* name = */ "queries",
                        /* src_path = */ "/Users/aw/Desktop/workspace/typedb-client-rust/tests/queries.rs",
                        /* crate_types = */ Arrays.asList("bin"),
                        /* edition = */ "2021",
                        /* doctest = */ false,
                        /* required_features = */ null
                ),
                /* profile = */ new Profile(/* test = */ true),
                /* filenames = */ Arrays.asList("/private/var/tmp/_bazel_aw/2d06d1855e62b9739437a06fc0ad8310/execroot/__main__/bazel-out/darwin-fastbuild/bin/tests/test-1315434595/queries"),
                /* executable = */ "/private/var/tmp/_bazel_aw/2d06d1855e62b9739437a06fc0ad8310/execroot/__main__/bazel-out/darwin-fastbuild/bin/tests/test-1315434595/queries"
//                /* filenames = */ Arrays.asList("/Users/aw/Desktop/workspace/typedb-client-rust/target/debug/deps/queries-4496e728abbb2007"),
//                /* executable = */ "/Users/aw/Desktop/workspace/typedb-client-rust/target/debug/deps/queries-4496e728abbb2007"
        );
//        env.putUserData(ARTIFACTS_KEY, new CompletableFuture());
//        env.getUserData(ARTIFACTS_KEY).complete(Arrays.asList(compilerArtifact));
        RsExecutableRunner.Companion.setArtifacts(env, Arrays.asList(compilerArtifact));
        System.out.println("executionInfo: executable = " + executionInfo.executable);
//        System.out.println("artifacts(from getUserData direct) = " + env.getUserData(ARTIFACTS_KEY).get());
        System.out.println("artifacts = " + RsExecutableRunner.Companion.getArtifacts(env));
      return super.doExecute(nativeState.cargoRunState, nativeState.environment);
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
