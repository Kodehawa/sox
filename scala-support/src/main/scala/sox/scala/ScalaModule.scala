package sox.scala

import sox.Sox
import sox.command.{AbstractCommand, AbstractContext, ReflectiveCommandManager}

object ScalaModule {
    def register(sox: Sox): Unit = {
        sox.commandManager() match {
            case manager: ReflectiveCommandManager[_, _, _] => registerFinder(manager)
            case _ => /* do nothing */
        }
    }

    private def registerFinder[C <: AbstractContext[C], T <: AbstractCommand[C, T]](manager: ReflectiveCommandManager[_, C, T]): Unit = {
        manager.addSubcommandFinder(new Finder())
    }
}