package com.filtro.ligacoes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.UserManager
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * O Android chama onScreenCall() em cada ligação enquanto este app for o
 * "app de filtro de chamadas". Se algo der errado (sem permissão, contatos
 * ainda criptografados, erro na consulta) a ligação PASSA: é melhor tocar uma
 * ligação indevida do que barrar um contato seu.
 */
class CallFilterService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val response = try {
            decide(callDetails)
        } catch (_: Exception) {
            allowResponse()
        }
        respondToCall(callDetails, response)
    }

    private fun decide(details: Call.Details): CallResponse {
        // Só mexe em ligações recebidas.
        if (details.callDirection != Call.Details.DIRECTION_INCOMING) return allowResponse()

        val prefs = Prefs(applicationContext)
        if (!prefs.enabled) return allowResponse()

        val handle = details.handle
        val isVoip = ((handle != null) && (handle.scheme != "tel"))

        if (isVoip) {
            if (!prefs.blockVoip) return allowResponse()
            val number = handle.schemeSpecificPart.orEmpty()
            val isKnown = number.isNotBlank() && (isContact(number) == true)
            if (isKnown) return allowResponse()

            prefs.addLog(number.ifBlank { "WhatsApp / VoIP" })
            return rejectResponse()
        }

        val number = handle?.schemeSpecificPart.orEmpty()

        if (!prefs.blockUnknown) return allowResponse()

        val shouldBlock = when (isContact(number)) {
            true -> false
            false -> true
            null -> false // não deu para consultar os contatos: deixa tocar
        }
        if (!shouldBlock) return allowResponse()

        prefs.addLog(number)
        return rejectResponse()
    }

    private fun allowResponse(): CallResponse =
        CallResponse.Builder().build()

    //
    private fun rejectResponse(): CallResponse =
        CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()

    /** true = é contato, false = não é, null = não foi possível consultar. */
    private fun isContact(number: String): Boolean? {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        val um = getSystemService(UserManager::class.java)
        if ((um != null) && !um.isUserUnlocked) return null

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number),
            )
            contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null,
            )?.use { it.count > 0 }
        } catch (_: Exception) {
            null
        }
    }
}
