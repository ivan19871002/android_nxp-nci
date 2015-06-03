/****************************************************************************
 ****************************************************************************
 ***
 ***   This header was automatically generated from a Linux kernel header
 ***   of the same name, to make information necessary for userspace to
 ***   call into the kernel available to libc.  It contains only constants,
 ***   structures, and macros generated from the original header, and thus,
 ***   contains no copyrightable information.
 ***
 ***   To edit the content of this header, modify the corresponding
 ***   source file (e.g. under external/kernel-headers/original/) then
 ***   run bionic/libc/kernel/tools/update_all.py
 ***
 ***   Any manual change here will be lost the next time this script will
 ***   be run. You've been warned!
 ***
 ****************************************************************************
 ****************************************************************************/
#define PN544_MAGIC 0xE9
#define PWR_OFF 0
#define PWR_ON 1
#define PWR_FW 2
/* WARNING: DO NOT EDIT, AUTO-GENERATED CODE - SEE TOP FOR INSTRUCTIONS */
#define CLK_OFF 0
#define CLK_ON 1
#define PN544_SET_PWR _IOW(PN544_MAGIC, 0x01, unsigned int)
#define PN54X_CLK_REQ _IOW(PN544_MAGIC, 0x02, unsigned int)
/* WARNING: DO NOT EDIT, AUTO-GENERATED CODE - SEE TOP FOR INSTRUCTIONS */
struct pn544_i2c_platform_data {
 unsigned int irq_gpio;
 unsigned int ven_gpio;
 unsigned int firm_gpio;
/* WARNING: DO NOT EDIT, AUTO-GENERATED CODE - SEE TOP FOR INSTRUCTIONS */
 unsigned int clkreq_gpio;
 struct regulator *pvdd_reg;
 struct regulator *vbat_reg;
 struct regulator *pmuvcc_reg;
/* WARNING: DO NOT EDIT, AUTO-GENERATED CODE - SEE TOP FOR INSTRUCTIONS */
 struct regulator *sevdd_reg;
};
