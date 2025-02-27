package com.app.imagevideoactivity.ui

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.imagevideoactivity.MainActivity
import com.app.imagevideoactivity.adapter.DriveFilesAdapter
import com.app.imagevideoactivity.databinding.FragmentDisplayBinding
import com.app.imagevideoactivity.model.DriveFile
import com.google.api.services.drive.Drive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TransferFragment : Fragment() {

    private var _binding: FragmentDisplayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentDisplayBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAndDisplayDriveFiles()
    }
    private fun fetchAndDisplayDriveFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            val files = fetchDriveFiles((activity as MainActivity).driveService!!)
            withContext(Dispatchers.Main) {
                if (files.isNotEmpty()) {
                    val adapter = DriveFilesAdapter(requireContext(), files){
                        file ->
                        lifecycleScope.launch {
                            val fileName = "downloaded_file${System.currentTimeMillis()}.jpg" // Change this dynamically
                            val outputPath = getOutputPath(requireContext(), fileName)
                            downloadFileFromDrive((activity as MainActivity).driveService!!, file.id, outputPath)
                        }
                    }
                    binding.recyclerView.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "No files found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchDriveFiles(driveService: Drive): List<DriveFile> {
        return withContext(Dispatchers.IO) {
            try {
                val fileList = driveService.Files().list()

                    .setFields("files(id, name, mimeType,  webViewLink, webContentLink, thumbnailLink)") // Fetch webContentLink
                    .execute()
                fileList.files?.map {
                    DriveFile(it.id, it.name, it.mimeType,it.webViewLink, it.webContentLink,it.thumbnailLink)
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    private fun getOutputPath(context: Context, fileName: String): String {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(directory, fileName).absolutePath
    }


    private suspend fun downloadFileFromDrive(driveService: Drive, fileId: String, outputPath: String) {
        withContext(Dispatchers.IO) {
            try {
                val outputStream = FileOutputStream(outputPath)
                driveService.Files().get(fileId).executeMediaAndDownloadTo(outputStream)
                outputStream.close()
                Log.d("DriveDownload", "File downloaded successfully: $outputPath")
            } catch (e: Exception) {
                Log.e("DriveDownload", "Download failed: ${e.message}")
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}