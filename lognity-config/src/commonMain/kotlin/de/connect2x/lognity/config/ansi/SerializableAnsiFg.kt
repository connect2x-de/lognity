package de.connect2x.lognity.config.ansi

import de.connect2x.lognity.api.ansi.AnsiFg
import kotlinx.serialization.Serializable

@Serializable
enum class SerializableAnsiFg(val value: AnsiFg) {
    // @formatter:off
    DEFAULT  (AnsiFg.default),
    BLACK    (AnsiFg.black),
    RED      (AnsiFg.red),
    GREEN    (AnsiFg.green),
    YELLOW   (AnsiFg.yellow),
    BLUE     (AnsiFg.blue),
    PURPLE   (AnsiFg.purple),
    CYAN     (AnsiFg.cyan),
    WHITE    (AnsiFg.white),
    HI_BLACK (AnsiFg.hiBlack),
    HI_RED   (AnsiFg.hiRed),
    HI_GREEN (AnsiFg.hiGreen),
    HI_YELLOW(AnsiFg.hiYellow),
    HI_BLUE  (AnsiFg.hiBlue),
    HI_PURPLE(AnsiFg.hiPurple),
    HI_CYAN  (AnsiFg.hiCyan),
    HI_WHITE (AnsiFg.hiWhite);
    // @formatter:on
}