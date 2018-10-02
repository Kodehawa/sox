package sox.scala

import sox.Sox
import sox.command.{AbstractContext, ReflectiveCommandManager}

object ScalaModule {
    def register(sox: Sox): Unit = {
        sox.commandManager() match {
            case manager: ReflectiveCommandManager[_, _] => registerFinder(manager)
            case _ => /* do nothing */
        }
    }

    private def registerFinder[C <: AbstractContext[C]](manager: ReflectiveCommandManager[_, C]): Unit = {
        manager.addSubcommandFinder(new Finder())
    }
}