/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.activiti.android.app.fragments.task;

import java.util.Map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.activiti.android.app.R;
import com.activiti.android.app.activity.MainActivity;
import com.activiti.android.platform.provider.transfer.ContentTransferSyncAdapter;
import com.activiti.android.platform.provider.transfer.DownloadTransferEvent;
import com.activiti.android.sdk.model.runtime.ParcelTask;
import com.activiti.android.ui.fragments.builder.LeafFragmentBuilder;
import com.activiti.android.ui.fragments.task.form.TaskFormFoundationFragment;
import com.activiti.android.ui.utils.UIUtils;
import com.activiti.client.api.model.runtime.TaskRepresentation;
import com.squareup.otto.Subscribe;

public class TaskFormFragment extends TaskFormFoundationFragment
{
    public static final String TAG = TaskFormFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public TaskFormFragment()
    {
        super();
    }

    public static TaskFormFragment newInstanceByTemplate(Bundle b)
    {
        TaskFormFragment cbf = new TaskFormFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onStart()
    {
        super.onStart();
        UIUtils.displayActionBarBack((MainActivity) getActivity(), getToolbar());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        UIUtils.setActionBarDefault((MainActivity) getActivity(), getToolbar());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onDownloadTransferEvent(DownloadTransferEvent event)
    {
        if (event.hasException)
        {
            Snackbar.make(getActivity().findViewById(R.id.left_panel), event.exception.getMessage(),
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        hideWaiting();

        try
        {
            switch (event.mode)
            {
                case ContentTransferSyncAdapter.MODE_SHARE:
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, event.data.getName());
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(event.data));
                    sendIntent.setType(event.mimetype);
                    getActivity().startActivity(
                            Intent.createChooser(sendIntent, getResources().getText(R.string.action_send_file)));
                    break;
                case ContentTransferSyncAdapter.MODE_OPEN_IN:
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.putExtra(Intent.EXTRA_SUBJECT, event.data.getName());
                    viewIntent.setDataAndType(Uri.fromFile(event.data), event.mimetype);
                    startActivity(viewIntent);
                    break;
            }
        }
        catch (ActivityNotFoundException e)
        {
            Snackbar.make(getActivity().findViewById(R.id.left_panel), R.string.file_editor_error_open,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        public Builder task(TaskRepresentation task)
        {
            extraConfiguration.putParcelable(ARGUMENT_TASK, new ParcelTask(task));
            return this;
        }

        public Builder bindFragmentTag(String fragmentListTag)
        {
            extraConfiguration.putString(ARGUMENT_BIND_FRAGMENT_TAG, fragmentListTag);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
