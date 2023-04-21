package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModCommentData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemCommentBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.snackbar.Snackbar

/**
 * 评论
 * @author Cold Mint
 * @date 2021/12/12 20:50
 */
class CommentAdapter(context: Context, dataList: MutableList<WebModCommentData.Data>) :
    BaseAdapter<ItemCommentBinding, WebModCommentData.Data>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemCommentBinding {
        return ItemCommentBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModCommentData.Data,
        viewBinding: ItemCommentBinding,
        viewHolder: ViewHolder<ItemCommentBinding>,
        position: Int
    ) {
        val icon = data.headIcon
        if (icon != null) {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon))
                .apply(GlobalMethod.getRequestOptions(true))
                .into(viewBinding.iconView)
        }
        viewBinding.nameView.text = data.userName
        viewBinding.timeView.text = if (data.location == null) {
            data.time
        } else {
            data.time + " " + data.location
        }
        viewBinding.thumbUpImageView.setOnClickListener {
            Snackbar.make(viewBinding.thumbUpImageView,R.string.temporarily_unavailable,Snackbar.LENGTH_SHORT).show()
        }
        viewBinding.shareImageView.setOnClickListener {
            AppOperator.shareText(context, context.getString(R.string.share_message), data.content);
        }
        viewBinding.moreImageView.setOnClickListener { view ->
            val menu = GlobalMethod.createPopMenu(view)
            menu.menu.add(R.string.copy)
            menu.menu.add(R.string.delete_title)
            menu.setOnMenuItemClickListener {
                val title = it.title
                when (title) {
                    context.getString(R.string.copy) -> {
                        GlobalMethod.copyText(context, data.content, view)
                    }
                    context.getString(R.string.delete_title) -> {
                        CoreDialog(context).setTitle(R.string.delete_comment).setMessage(
                            String.format(
                                context.getString(R.string.delete_comment_tip),
                                data.userName
                            )
                        ).setPositiveButton(R.string.dialog_ok){
                            val token = AppSettings.getValue(AppSettings.Setting.Token, "")
                            if (token.isNullOrBlank()) {
                                Snackbar.make(
                                    view,
                                    context.getString(R.string.please_login_first),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                WebMod.instance.modifyCommentVisibility(
                                    token,
                                    data.id,
                                    object : ApiCallBack<ApiResponse> {
                                        override fun onResponse(t: ApiResponse) {
                                            if (t.code == ServerConfiguration.Success_Code) {
                                                removeItem(viewHolder.adapterPosition)
                                                Snackbar.make(
                                                    view,
                                                    t.message,
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Snackbar.make(
                                                    viewBinding.root,
                                                    t.message,
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(e: Exception) {
                                            Snackbar.make(
                                                viewBinding.root,
                                                R.string.network_error,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }
                        }.setNegativeButton(R.string.dialog_cancel){

                        }.show()
                    }
                }
                true
            }
            menu.show()
        }
        TextStyleMaker.instance.load(viewBinding.contentView, data.content) { type, data ->
            TextStyleMaker.instance.clickEvent(context, type, data)
        }
    }


}