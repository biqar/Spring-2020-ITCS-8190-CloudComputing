package utils

class CommonFunctions {
  
  val commonStrings = new StringsDAO()
  
  /*****************Checks empty values in a list*****************/
  def checkPresenceOfEmptyValues(list : List[String]) : Boolean = {
    list.contains(commonStrings.EMPTY_STRING) ||
                            list.contains(commonStrings.FILE_EMPTY_VALUE)
  }
}