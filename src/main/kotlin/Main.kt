fun main(args: Array<String>) {
    try {
        if (args.isEmpty()) {
            System.err.println("Add path to the variable!")
        } else {

            val editor = JsonEditor().apply {
                addField("BroadbandAccessConnectedAlready", "installmentMonthsNumberText", "")
                addField("BroadbandAccessConnectedAlready", "changeSpeedBlockText", "")
                addField("BroadbandAccessConnectedAlready", "subscriptionFeeText", "")
                addField("BroadbandAccessConnectedAlready", "notChangeSpeedInternetTitle", "")
                addField("BroadbandAccessConnectedAlready", "notChangeSpeedInternetText", "")
                addField("BroadbandAccessConnectedAlready", "internetSpeedTitle", "")
                addField("BroadbandAccessConnectedAlready", "subscriptionInternetSpeedText", "")
                addField("BroadbandAccessConnectedAlready", "subscriptionInternetCostText", "")
                addField("BroadbandAccessConnectedAlready", "saveChangesPopupTitle", "")
                addField("BroadbandAccessConnectedAlready", "newSpeedPopupText", "")
                addField("BroadbandAccessConnectedAlready", "newCostPopupText", "")
                addField("BroadbandAccessConnectedAlready", "finalPopupTitle", "")
                addField("BroadbandAccessConnectedAlready", "finalPopupText", "")
                addField("BroadbandAccessConnectedAlready", "speedSelectionErrorText", "")
            }
            println(editor.processContent(args[0]))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
