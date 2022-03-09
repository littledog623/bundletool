/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tools.build.bundletool.validation;

import com.android.tools.build.bundletool.model.SdkBundle;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.zip.ZipFile;

/** Validates the files and configuration for the SDK bundle. */
public class SdkBundleValidator {

  /** Validators run on the bundle zip file. */
  @VisibleForTesting
  static final ImmutableList<SubValidator> DEFAULT_BUNDLE_FILE_SUB_VALIDATORS =
      // Keep order of common validators in sync with BundleModulesValidator.
      ImmutableList.of(
          new BundleZipValidator(),
          new MandatoryFilesPresenceValidator(SdkBundle.NON_MODULE_DIRECTORIES),
          new SdkBundleHasOneModuleValidator());

  /** Validators run on the internal representation of bundle and bundle modules. */
  @VisibleForTesting
  static final ImmutableList<SubValidator> DEFAULT_BUNDLE_SUB_VALIDATORS =
      // Keep order of common validators in sync with BundleModulesValidator.
      ImmutableList.of(
          // Fundamental file validations first.
          new BundleFilesValidator(),
          new SdkBundleModuleNameValidator(),
          // More specific file validations.
          new DexFilesValidator(),
          new SdkAndroidManifestValidator(),
          // Other.
          new ResourceTableValidator());

  private final ImmutableList<SubValidator> allBundleFileSubValidators;
  private final ImmutableList<SubValidator> allBundleSubValidators;

  private SdkBundleValidator(
      ImmutableList<SubValidator> allBundleSubValidators,
      ImmutableList<SubValidator> allBundleFileSubValidators) {
    this.allBundleSubValidators = allBundleSubValidators;
    this.allBundleFileSubValidators = allBundleFileSubValidators;
  }

  public static SdkBundleValidator create() {
    return create(ImmutableList.of());
  }

  public static SdkBundleValidator create(ImmutableList<SubValidator> extraSubValidators) {
    return new SdkBundleValidator(
        ImmutableList.<SubValidator>builder()
            .addAll(DEFAULT_BUNDLE_SUB_VALIDATORS)
            .addAll(extraSubValidators)
            .build(),
        ImmutableList.<SubValidator>builder()
            .addAll(DEFAULT_BUNDLE_FILE_SUB_VALIDATORS)
            .addAll(extraSubValidators)
            .build());
  }

  /** Validates the given Sdk Bundle zip file. */
  public void validateFile(ZipFile bundleFile) {
    new ValidatorRunner(allBundleFileSubValidators).validateBundleZipFile(bundleFile);
  }

  /** Validates the given Sdk Bundle. */
  public void validate(SdkBundle bundle) {
    new ValidatorRunner(allBundleSubValidators).validateSdkBundle(bundle);
  }
}
