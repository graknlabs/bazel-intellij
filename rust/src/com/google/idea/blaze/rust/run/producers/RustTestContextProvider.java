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
package com.google.idea.blaze.rust.run.producers;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.idea.blaze.base.dependencies.TargetInfo;
import com.google.idea.blaze.base.run.ExecutorType;
import com.google.idea.blaze.base.run.TestTargetHeuristic;
import com.google.idea.blaze.base.run.producers.RunConfigurationContext;
import com.google.idea.blaze.base.run.producers.TestContext;
import com.google.idea.blaze.base.run.producers.TestContextProvider;
import com.google.idea.blaze.rust.run.RustTestUtils;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import javax.annotation.Nullable;
import org.rust.lang.core.psi.RsFile;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.ext.RsFunctionKt;

class RustTestContextProvider implements TestContextProvider {
  @Nullable
  @Override
  public RunConfigurationContext getTestContext(ConfigurationContext context) {
    PsiElement element = context.getPsiLocation();
    if (element == null) {
      return null;
    }
    TestLocation testLocation = testLocation(element);
    if (testLocation == null) {
      return null;
    }
    ListenableFuture<TargetInfo> target =
        TestTargetHeuristic.targetFutureForPsiElement(element, /* testSize= */ null);
    if (target == null) {
      return null;
    }
    return TestContext.builder(testLocation.sourceElement(), ExecutorType.DEBUG_SUPPORTED_TYPES)
            .setTarget(target)
            .build();
  }

    @Nullable
    private static TestLocation testLocation(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof RsFile) || !RustTestUtils.isTestFile((RsFile) file)) {
            return null;
        }
        RsFunction rsFunction = PsiTreeUtil.getParentOfType(element, RsFunction.class, false);
        if (rsFunction != null && RsFunctionKt.isTest(rsFunction)) {
            return new TestLocation((RsFile) file, rsFunction);
        }
        return new TestLocation((RsFile) file, null);
    }

    private static class TestLocation {
        private final RsFile testFile;
        @Nullable private final RsFunction testFunction;

        private TestLocation(RsFile testFile, @Nullable RsFunction testFunction) {
            this.testFile = testFile;
            this.testFunction = testFunction;
        }

        PsiElement sourceElement() {
            return testFunction != null ? testFunction : testFile;
        }
    }
}
