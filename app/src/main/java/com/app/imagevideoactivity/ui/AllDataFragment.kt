package com.app.imagevideoactivity.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.app.imagevideoactivity.MainActivity
import com.app.imagevideoactivity.adapter.MediaAdapter
import com.app.imagevideoactivity.databinding.FragmentHomeBinding
import com.app.imagevideoactivity.model.MediaItem
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class AllDataFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var list: ArrayList<MediaItem> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressCircular.isVisible = true
        requestPermission(requireContext())



    }

    private fun getAllMedia(context: Context): ArrayList<MediaItem> {
        val mediaList = ArrayList<MediaItem>()
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MEDIA_TYPE)
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
        val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(), MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, selection, selectionArgs, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val path = it.getString(0)
                val type = it.getInt(1)
                mediaList.add(MediaItem(path, type , type==3))
            }
        }
        return mediaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun requestPermission(context: Context) {
        val array = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        Dexter.withContext(context)
            .withPermissions(
                array
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        /*list.addAll(getData(requireContext()))
                        list.addAll(getVideoList(requireContext()))*/
                        list.addAll(getAllMedia(requireContext()))
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressCircular.isVisible = false
                                val adapter = MediaAdapter(list){ mediaItem ->
                                    binding.progressCircular.isVisible = true
                                    uploadFileToDrive(File(mediaItem.path), (activity as MainActivity).driveService!!, requireContext())
                                }
                                binding.recyclerView.adapter = adapter
                            }
                        }

                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        // check for permanent denial of any permission
                        // show alert dialog navigating to Settings
                        showSettingsDialog(context)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error ->

            }
            .onSameThread()
            .check()
    }
    //open setting dialog
    fun showSettingsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings(context)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()

    }

    //open setting screen
    private fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        resultLauncherSetting.launch(intent)
    }

    private var resultLauncherSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    fun uploadFileToDrive(fileUri: File, driveService: Drive, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val metadata = com.google.api.services.drive.model.File().setName(fileUri.name)
                val mediaContent = FileContent("image/jpeg", fileUri) // Change to "video/mp4" for videos

                val request = driveService.Files().create(metadata, mediaContent)
                    .setFields("id")
                    .execute()

                withContext(Dispatchers.Main) {
                    binding.progressCircular.isVisible = false
                    if (request.id != null) {
                        Toast.makeText(context, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressCircular.isVisible = false
                    Toast.makeText(context, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}