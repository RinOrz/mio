@file:Suppress("SpellCheckingInspection")

import com.meowool.toolkit.sweekt.isJvmArrayTypeDescriptor
import com.meowool.toolkit.sweekt.isJvmTypeDescriptor
import com.meowool.toolkit.sweekt.isJvmPrimitiveType
import com.meowool.toolkit.sweekt.isJvmPrimitiveTypeDescriptor
import com.meowool.toolkit.sweekt.toJvmPackageName
import com.meowool.toolkit.sweekt.toJvmQualifiedTypeName
import com.meowool.toolkit.sweekt.toJvmTypeDescriptor
import com.meowool.toolkit.sweekt.toJvmTypeSimpleName
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class JvmDescriptorTestsJVM : StringSpec({
  "isJvmArrayTypeDescriptor" {
    Array<String>::class.java.name should {
      it shouldBe "[Ljava.lang.String;"
      it.isJvmArrayTypeDescriptor() shouldBe true
    }
    Array<Array<LongArray>>::class.java.name should {
      it shouldBe "[[[J"
      it.isJvmArrayTypeDescriptor() shouldBe true
    }
    String::class.java.name.isJvmArrayTypeDescriptor() shouldBe false
  }
  "isJvmDescriptor" {
    Map.Entry::class.java.name.toJvmTypeDescriptor('.') should {
      it shouldBe "Ljava.util.Map\$Entry;"
      it.isJvmTypeDescriptor() shouldBe true
    }
  }
})