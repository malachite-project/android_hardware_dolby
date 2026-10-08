#
# Copyright (C) 2022 FlamingoOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Control app only. The device supplies the Dolby DAX vendor stack (DAP
# effect with implementation UUID 9d4921da-8225-4f29-aefa-39537a04bcaa,
# vendor.dolby.hardware.dms@2.0 service, its SELinux policy and
# audio_effects.xml entries).

DOLBY_PATH := hardware/dolby

PRODUCT_SOONG_NAMESPACES +=     $(DOLBY_PATH)

PRODUCT_PACKAGES +=     LunarisDolby

PRODUCT_COPY_FILES +=     $(DOLBY_PATH)/configs/permissions/privapp-permissions-dolby.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-dolby.xml
