package com.authguidance.basicmobileapp.views.dialogs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.databinding.FragmentErrorDetailsBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorReporter
import com.authguidance.basicmobileapp.views.activities.MainActivity
import com.authguidance.basicmobileapp.views.adapters.ErrorItemArrayAdapter

/*
 * A custom modal dialog based on the error details fragment
 */
class ErrorDetailsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentErrorDetailsBinding
    private lateinit var mainActivity: MainActivity
    private var title: String? = null
    private var error: UIError? = null

    /*
     * The factory method to create the dialog
     */
    companion object {
        fun create(dialogTitle: String, error: UIError): ErrorDetailsDialogFragment {
            val dialog = ErrorDetailsDialogFragment()
            val args = Bundle()
            args.putString(Constants.ARG_ERROR_TITLE, dialogTitle)
            args.putSerializable(Constants.ARG_ERROR_DATA, error)
            dialog.arguments = args
            return dialog
        }
    }

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Inflate the view when created
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get data passed in
        this.title = this.arguments?.getString(Constants.ARG_ERROR_TITLE)
        this.error = this.arguments?.getSerializable(Constants.ARG_ERROR_DATA) as UIError

        // Inflate the layout
        this.binding = FragmentErrorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Initialise the error view from data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the title
        this.binding.fragmentHeadingText.text = title

        // Dismiss when the X icon is clicked
        this.binding.dismiss.setOnClickListener {
            this.dismiss()
        }

        // Render the error object
        this.renderData()
    }

    /*
     * Render error items in the list view
     */
    private fun renderData() {

        val context = this.context
        val error = this.error
        if (error != null && context != null) {

            val lines = ErrorReporter(context).getErrorLines(error)

            val list = this.binding.listErrorItems
            list.layoutManager = LinearLayoutManager(this.mainActivity)
            list.adapter = ErrorItemArrayAdapter(mainActivity, lines)
            list.adapter = ErrorItemArrayAdapter(mainActivity, lines)
        }
    }
}
