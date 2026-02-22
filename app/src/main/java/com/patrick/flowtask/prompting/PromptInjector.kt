package com.patrick.flowtask.prompting

class PromptInjector {
    fun inject(template: PromptTemplate, context: ContextState, userInput: String): String {
        return template.promptStructure
            .replace("{activeTaskCount}", context.activeTaskCount.toString())
            .replace("{timeOfDay}", context.timeOfDay)
            .replace("{userInput}", userInput)
    }
}
