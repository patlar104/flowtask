package com.patrick.flowtask.app

import android.content.Context
import com.patrick.flowtask.BuildConfig
import com.patrick.flowtask.data.PersistentTaskRepository
import com.patrick.flowtask.data.TaskRepository
import com.patrick.flowtask.domain.ConversationUseCase
import com.patrick.flowtask.domain.FakeRuntimeAiClient
import com.patrick.flowtask.domain.HttpRuntimeAiClient
import com.patrick.flowtask.domain.RuntimeAiClient
import com.patrick.flowtask.domain.SelectingRuntimeAiClient
import com.patrick.flowtask.prompting.PromptInjector
import com.patrick.flowtask.prompting.StructuredResponseParser

class AppContainer(
    private val context: Context
) {
    val taskRepository: TaskRepository by lazy { PersistentTaskRepository(context) }
    val runtimeAiClient: RuntimeAiClient by lazy {
        SelectingRuntimeAiClient(
            useFakeClient = BuildConfig.USE_FAKE_AI,
            fakeClient = FakeRuntimeAiClient(),
            httpClient = HttpRuntimeAiClient(
                backendUrl = BuildConfig.AI_BACKEND_URL
            )
        )
    }
    val conversationUseCase: ConversationUseCase by lazy {
        ConversationUseCase(
            aiClient = runtimeAiClient,
            promptInjector = PromptInjector(),
            parser = StructuredResponseParser()
        )
    }
}
