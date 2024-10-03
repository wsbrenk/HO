package core.model

import core.db.AbstractTable.Storable
import core.db.DBManager

open class HOConfigurationParameter(val key: String, defaultValue: String?) : Storable() {
    private var value: String?

    init {
        this.value = parameters.getProperty(key)
        if (value == null) {
            value = DBManager.instance().loadHOConfigurationParameter(key)
            if (value == null) {
                value = defaultValue
            }
            parameters.setProperty(key, value)
        }
    }

    fun getValue(): String? {
        return value
    }

    fun setValue(value: String) {
        if (value != this.value) {
            this.value = value
            parameters.setProperty(key, value)
            parametersChanged = true
        }
    }

    companion object {
        @JvmStatic
        protected val parameters: HOProperties = HOProperties()
        private var parametersChanged = false

        @JvmStatic
        fun storeParameters() {
            if (parametersChanged) {
                for ((key, value) in parameters) {
                    DBManager.instance().saveUserParameter(key as String, value as String)
                }
            }
        }
    }
}


