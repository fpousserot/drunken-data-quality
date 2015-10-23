package de.frosner.ddq.core

import de.frosner.ddq.reporters.Reporter


/**
 * An object responsible for running checks and producing reports
 */
object Runner {

  /**
   * Run checks and then report to the reporters. Each check will be reported by every reporter.
   *
   * @param checks An iterable of check objects to be reported
   * @param reporters An iterable of reporters
   * @return Result for every check passed as an argument
   */
  def run(checks: Iterable[Check], reporters: Iterable[Reporter]): Iterable[CheckResult] = {
    checks.map(check => {
      val potentiallyPersistedDf = check.cacheMethod.map(check.dataFrame.persist(_)).getOrElse(check.dataFrame)

      val header = s"Checking ${check.displayName.getOrElse(check.dataFrame.toString)}"
      val prologue = s"It has a total number of ${potentiallyPersistedDf.columns.length} columns " +
        s"and ${potentiallyPersistedDf.count} rows."
      val constraintResults = check.constraints.map(c => (c, c.fun(potentiallyPersistedDf))).toMap

      val checkResult = CheckResult(header, prologue, constraintResults, check)

      if (check.cacheMethod.isDefined) potentiallyPersistedDf.unpersist()

      reporters.foreach(_.report(checkResult))
      checkResult
    })
  }

}