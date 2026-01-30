package com.assembled.chat.network

import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.ChatError
import kotlin.test.*

class MessageBridgeTest {

    private var readyCalled = false
    private var openCalled = false
    private var closeCalled = false
    private var errorCalled = false
    private var debugCalled = false
    private var newMessageCalled = false
    private var capturedError: ChatError? = null
    private var capturedDebugMessage: String? = null
    private var capturedMessageCount: Int? = null

    private val testListener = object : AssembledChatListener {
        override fun onChatReady() {
            readyCalled = true
        }

        override fun onChatOpened() {
            openCalled = true
        }

        override fun onChatClosed() {
            closeCalled = true
        }

        override fun onError(error: ChatError) {
            errorCalled = true
            capturedError = error
        }

        override fun onDebug(message: String) {
            debugCalled = true
            capturedDebugMessage = message
        }

        override fun onNewMessage(messageCount: Int) {
            newMessageCalled = true
            capturedMessageCount = messageCount
        }
    }

    @BeforeTest
    fun setUp() {
        readyCalled = false
        openCalled = false
        closeCalled = false
        errorCalled = false
        debugCalled = false
        newMessageCalled = false
        capturedError = null
        capturedDebugMessage = null
        capturedMessageCount = null
    }

    @Test
    fun `onReady should call listener onChatReady`() {
        val bridge = MessageBridge(testListener, debug = false)
        
        bridge.onReady()
        
        assertTrue(readyCalled, "onChatReady should be called")
    }

    @Test
    fun `onOpen should call listener onChatOpened`() {
        val bridge = MessageBridge(testListener, debug = false)
        
        bridge.onOpen()
        
        assertTrue(openCalled, "onChatOpened should be called")
    }

    @Test
    fun `onClose should call listener onChatClosed`() {
        val bridge = MessageBridge(testListener, debug = false)

        bridge.onOpen() // Must open first since close deduplicates by state
        closeCalled = false // Reset after open
        bridge.onClose()

        assertTrue(closeCalled, "onChatClosed should be called")
    }

    @Test
    fun `onError should call listener with BridgeError`() {
        val bridge = MessageBridge(testListener, debug = false)
        val errorMessage = "Test error message"
        
        bridge.onError(errorMessage)
        
        assertTrue(errorCalled, "onError should be called")
        assertNotNull(capturedError, "Error should be captured")
        assertTrue(capturedError is ChatError.BridgeError, "Error should be BridgeError type")
        assertEquals(errorMessage, capturedError?.message)
    }

    @Test
    fun `onDebug should call listener with message`() {
        val bridge = MessageBridge(testListener, debug = true)
        val debugMessage = "Test debug message"
        
        bridge.onDebug(debugMessage)
        
        assertTrue(debugCalled, "onDebug should be called")
        assertEquals(debugMessage, capturedDebugMessage)
    }

    @Test
    fun `onNewMessage should call listener with count`() {
        val bridge = MessageBridge(testListener, debug = false)
        val messageCount = 5
        
        bridge.onNewMessage(messageCount)
        
        assertTrue(newMessageCalled, "onNewMessage should be called")
        assertEquals(messageCount, capturedMessageCount)
    }

    @Test
    fun `postMessage with ready type should call onReady`() {
        val bridge = MessageBridge(testListener, debug = false)
        
        bridge.postMessage("ready", "")
        
        assertTrue(readyCalled, "onChatReady should be called for 'ready' message type")
    }

    @Test
    fun `postMessage with open type should call onOpen`() {
        val bridge = MessageBridge(testListener, debug = false)
        
        bridge.postMessage("open", "")
        
        assertTrue(openCalled, "onChatOpened should be called for 'open' message type")
    }

    @Test
    fun `postMessage with close type should call onClose`() {
        val bridge = MessageBridge(testListener, debug = false)

        bridge.postMessage("open", "") // Must open first since close deduplicates by state
        closeCalled = false
        bridge.postMessage("close", "")

        assertTrue(closeCalled, "onChatClosed should be called for 'close' message type")
    }

    @Test
    fun `postMessage with error type should call onError`() {
        val bridge = MessageBridge(testListener, debug = false)
        val errorData = "Error data"
        
        bridge.postMessage("error", errorData)
        
        assertTrue(errorCalled, "onError should be called for 'error' message type")
        assertEquals(errorData, capturedError?.message)
    }

    @Test
    fun `postMessage with debug type should call onDebug`() {
        val bridge = MessageBridge(testListener, debug = true)
        val debugData = "Debug data"
        
        bridge.postMessage("debug", debugData)
        
        assertTrue(debugCalled, "onDebug should be called for 'debug' message type")
        assertEquals(debugData, capturedDebugMessage)
    }

    @Test
    fun `postMessage with unknown type should not throw exception`() {
        val bridge = MessageBridge(testListener, debug = false)
        
        // Should not throw
        bridge.postMessage("unknown_type", "some data")
        
        assertFalse(readyCalled, "No listener methods should be called for unknown type")
        assertFalse(openCalled, "No listener methods should be called for unknown type")
        assertFalse(closeCalled, "No listener methods should be called for unknown type")
        assertFalse(errorCalled, "No listener methods should be called for unknown type")
    }

    @Test
    fun `bridge with null listener should not throw exception`() {
        val bridge = MessageBridge(null, debug = false)
        
        // Should not throw
        bridge.onReady()
        bridge.onOpen()
        bridge.onClose()
        bridge.onError("error")
        bridge.onDebug("debug")
        bridge.onNewMessage(1)
    }

    @Test
    fun `bridge with debug disabled should still call listener methods`() {
        val bridge = MessageBridge(testListener, debug = false)

        bridge.onReady()
        bridge.onOpen()
        bridge.onClose() // Close after open, so state tracking allows it

        assertTrue(readyCalled, "onChatReady should be called even with debug=false")
        assertTrue(openCalled, "onChatOpened should be called even with debug=false")
        assertTrue(closeCalled, "onChatClosed should be called even with debug=false")
    }

    @Test
    fun `bridge with debug enabled should call listener methods`() {
        val bridge = MessageBridge(testListener, debug = true)

        bridge.onReady()
        bridge.onDebug("test")

        assertTrue(readyCalled, "onChatReady should be called with debug=true")
        assertTrue(debugCalled, "onDebug should be called with debug=true")
    }

    @Test
    fun `duplicate onOpen calls should be deduplicated`() {
        var openCount = 0
        val countingListener = object : AssembledChatListener {
            override fun onChatOpened() { openCount++ }
        }
        val bridge = MessageBridge(countingListener, debug = false)

        bridge.onOpen()
        bridge.onOpen()
        bridge.onOpen()

        assertEquals(1, openCount, "onChatOpened should only be called once for duplicate opens")
    }

    @Test
    fun `duplicate onClose calls should be deduplicated`() {
        var closeCount = 0
        val countingListener = object : AssembledChatListener {
            override fun onChatClosed() { closeCount++ }
        }
        val bridge = MessageBridge(countingListener, debug = false)

        bridge.onOpen() // Must open first
        bridge.onClose()
        bridge.onClose()
        bridge.onClose()

        assertEquals(1, closeCount, "onChatClosed should only be called once for duplicate closes")
    }

    @Test
    fun `open and close cycle should work correctly`() {
        var openCount = 0
        var closeCount = 0
        val countingListener = object : AssembledChatListener {
            override fun onChatOpened() { openCount++ }
            override fun onChatClosed() { closeCount++ }
        }
        val bridge = MessageBridge(countingListener, debug = false)

        bridge.onOpen()
        bridge.onClose()
        bridge.onOpen()
        bridge.onClose()

        assertEquals(2, openCount, "onChatOpened should be called twice for two open/close cycles")
        assertEquals(2, closeCount, "onChatClosed should be called twice for two open/close cycles")
    }
}

