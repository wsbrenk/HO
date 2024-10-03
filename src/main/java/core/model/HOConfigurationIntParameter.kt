package core.model

class HOConfigurationIntParameter(key: String?, defaultValue: Int) :
    HOConfigurationParameter(key!!, defaultValue.toString()) {
    private var intValue: Int

    init {
        this.intValue = parameters.getInt(key, defaultValue)
    }

    fun getIntValue(): Int {
        return this.intValue
    }

    fun setIntValue(newValue: Int) {
        if (this.intValue != newValue) {
            this.intValue = newValue
            setValue(newValue.toString())
        }
    }
}