package com.example.toycamping.ui.mypage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.toycamping.App
import com.example.toycamping.R
import com.example.toycamping.base.BaseFragment
import com.example.toycamping.base.ViewState
import com.example.toycamping.data.model.NotificationItem
import com.example.toycamping.data.model.QuestionItem
import com.example.toycamping.databinding.DashboardFragmentBinding
import com.example.toycamping.ext.convertDate
import com.example.toycamping.ext.showDialog
import com.example.toycamping.ext.showToast
import com.example.toycamping.ui.adapter.NotificationAdapter
import com.example.toycamping.viewmodel.DashBoardViewModel

class DashboardFragment : BaseFragment<DashboardFragmentBinding>(R.layout.dashboard_fragment) {


    private val dashboardViewModel by viewModels<DashBoardViewModel>()

    private val notificationAdapter by lazy { NotificationAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = dashboardViewModel

        initUi()
        initViewModel()
    }

    private fun initViewModel() {
        dashboardViewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState: ViewState? ->
            (viewState as? DashBoardViewModel.DashBoardViewState)?.let {
                onChangedLoginViewState(
                    viewState
                )
            }
        }
    }

    private fun initUi() {
        dashboardViewModel.getUserInfo()

        binding.rvNotification.run {
            adapter = notificationAdapter
        }

        notificationAdapter.setOnItemClickListener {
            startNotificationDialog(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onChangedLoginViewState(viewState: DashBoardViewModel.DashBoardViewState) {
        when (viewState) {

            is DashBoardViewModel.DashBoardViewState.LogoutSuccess -> {
                showToast(App.instance.context(), "???????????? ??????.")
            }

            is DashBoardViewModel.DashBoardViewState.LogoutFailure -> {
                showToast(message = "???????????? ??????.")
            }

            is DashBoardViewModel.DashBoardViewState.WithdrawSuccess -> {
                showToast(App.instance.context(), "???????????? ??????.")
            }

            is DashBoardViewModel.DashBoardViewState.WithdrawFailure -> {
                showToast(message = "???????????? ??????.")
            }

            is DashBoardViewModel.DashBoardViewState.EmptyNotificationItem -> {
                showToast(message = "???????????? ??????.")
            }

            is DashBoardViewModel.DashBoardViewState.ErrorGetNotificationItem -> {
                showToast(message = "???????????? ??????????????? ?????????????????????.")
            }

            is DashBoardViewModel.DashBoardViewState.ShowLogoutDialog -> {
                showDialog(
                    title = "???????????? ????????????????",
                    detail = "??????????????? ????????????, ?????? ????????? ????????? ??? ?????????!",
                    titleButton = "????????????",
                    type = "logout"
                ) { _, result ->
                    val getResultType = result.getString(DialogFragment.TYPE)
                    dashboardViewModel.checkType(getResultType)
                }
            }

            is DashBoardViewModel.DashBoardViewState.ShowWithdrawDialog -> {
                showDialog(
                    title = "???????????? ????????????????",
                    detail = "??????????????? ???????????? ?????? ????????? ???????????????",
                    titleButton = "????????????",
                    type = "withdraw"
                ) { _, result ->
                    val getResultType = result.getString(DialogFragment.TYPE)
                    dashboardViewModel.checkType(getResultType)
                }
            }

            is DashBoardViewModel.DashBoardViewState.ShowNotification -> {
                viewNotification(isShow = true)
                notificationAdapter.getAll(viewState.list)
            }

            is DashBoardViewModel.DashBoardViewState.RouteDashboard -> {
                viewNotification(isShow = false)
                notificationAdapter.clear()
            }


            is DashBoardViewModel.DashBoardViewState.ShowQuestion -> {
                startAddQuestionDialog()
            }

            is DashBoardViewModel.DashBoardViewState.ShowIdentify -> {
                startIdentifyDialog()
            }

            is DashBoardViewModel.DashBoardViewState.AddQuestionFailure -> {
                showToast(message = "???????????? ????????? ?????????????????????.")
            }

            is DashBoardViewModel.DashBoardViewState.AddQuestionSuccess -> {
                showToast(message = "??????????????? ?????????????????????.")
            }

            is DashBoardViewModel.DashBoardViewState.GetUserInfo -> {
                with(binding) {
                    tvLoginId.text = viewState.id
                    tvLoginNickname.text = "${viewState.nickname}??? ???????????????."
                }
            }
        }
    }

    private fun startAddQuestionDialog() {
        val dialog = AddQuestionDialogFragment.newInstance(title = "????????????")
        dialog.show(parentFragmentManager, dialog::class.simpleName)

        parentFragmentManager.setFragmentResultListener(
            AddQuestionDialogFragment.SUBMIT,
            this
        ) { _: String, bundle: Bundle ->
            val getQuestionItem = bundle.getParcelable<QuestionItem>(AddQuestionDialogFragment.ITEM)
            dashboardViewModel.addQuestion(getQuestionItem)
        }
    }

    private fun startNotificationDialog(item: NotificationItem) {
        val dialog = DialogNotificationFragment.newInstance(
            title = item.title.orEmpty(),
            detail = item.detail.orEmpty(),
            date = item.date?.convertDate().orEmpty()
        )
        dialog.show(parentFragmentManager, dialog::class.simpleName)
    }


    private fun startIdentifyDialog() {
        val dialog = DialogIdentifyFragment.newInstance(title = "????????????????????????")
        dialog.show(parentFragmentManager, dialog::class.simpleName)
    }

    private fun viewNotification(isShow: Boolean) {
        binding.containerDashboard.isVisible = !isShow
        binding.containerNotification.isVisible = isShow
    }
}