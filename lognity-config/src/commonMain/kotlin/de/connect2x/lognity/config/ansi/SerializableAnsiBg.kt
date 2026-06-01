package de.connect2x.lognity.config.ansi

import de.connect2x.lognity.api.ansi.AnsiBg
import kotlinx.serialization.Serializable

@Serializable
enum class SerializableAnsiBg(val value: AnsiBg) {
    // @formatter:off
    DEFAULT  (AnsiBg.default),
    BLACK    (AnsiBg.black),
    RED      (AnsiBg.red),
    GREEN    (AnsiBg.green),
    YELLOW   (AnsiBg.yellow),
    BLUE     (AnsiBg.blue),
    PURPLE   (AnsiBg.purple),
    CYAN     (AnsiBg.cyan),
    WHITE    (AnsiBg.white),
    HI_BLACK (AnsiBg.hiBlack),
    HI_RED   (AnsiBg.hiRed),
    HI_GREEN (AnsiBg.hiGreen),
    HI_YELLOW(AnsiBg.hiYellow),
    HI_BLUE  (AnsiBg.hiBlue),
    HI_PURPLE(AnsiBg.hiPurple),
    HI_CYAN  (AnsiBg.hiCyan),
    HI_WHITE (AnsiBg.hiWhite);
    // @formatter:on
}